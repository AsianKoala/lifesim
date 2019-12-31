package GameComponents;

import Controls.KeyboardControls;
import Controls.MiscControlStuff;
import GameSession.GameLayout;
import Main.EventLoop;
import Util.MyMath;
import Util.WindowSize;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import static Controls.KeyboardControls.*;
import static java.lang.Math.*;


public class Player extends GameComponent {

    private static Player instance;

    /** Gets the player instance because there can only be one player character.
     * This is pretty much a singleton design pattern.
     *
     * @return first defined instance of player if one exists already.
     */
    public static Player getInstance() { return instance; }


    public void goTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void goTo(Structure structure) {
        goTo(structure.getX(), structure.getY());
        this.world = structure.getWorld();
    }


    private World world;

    public void goTo(World world) {
        goTo(0, 0);
        this.world = world;
    }

    public void goTo(int x, int y, World world) {
        goTo(x, y);
        this.world = world;
    }

    public World getWorld() { return world; }


    public String getWorldLabel() { return world.getLabel(); }


    public boolean isInSameWorld(Structure structure) {
        boolean sameWorld = false;
        if (structure.getWorld() == this.world)
            sameWorld = true;

        return sameWorld;
    }


    public int getDisplayX() {
        return WindowSize.getMidWidth()-getRadius();
    }

    public int getDisplayY() {
        return WindowSize.getMidHeight()-getRadius();
    }


    private int radius;

    public int getRadius() {
        return radius;
    }

    public int getDiameter() {
        return radius*2;
    }


    public Ellipse2D.Double getShape() { return new Ellipse2D.Double(getDisplayX(), getDisplayY(), getDiameter(), getDiameter()); }


    public ArrayList<Structure> getTouching() {
        ArrayList<Structure> touching = new ArrayList<>();

        for (Structure structure : Structure.getInstances()) {
            if (getShape().intersects(structure.getShape()) && isInSameWorld(structure)) {
                touching.add(structure);
            }
        }

        return touching;
    }


    public boolean isTouchingAnything() { return !getTouching().isEmpty(); }

    public Structure getTopTouching() {
        Structure topTouching = null;
        if (isTouchingAnything())
            topTouching = getTouching().get(0);

        return topTouching;
    }


    private double health = 1000;

    public double getHealth() { return health; }


    public void heal(double amount) { health += amount;health = MyMath.clamp(health, 0, 1000+strength); }


    private double energy = 1000;

    public double getEnergy() { return energy; }

    public void energize(double amount) { energy += amount; }

    public void tire(double amount) { energy -= amount; }


    private double money = 0;

    public double getMoney() { return money; }

    public boolean hasMoney() { return money > 0; }

    public boolean hasEnoughMoney(double amount) { return money > amount; }

    public void gainMoney(double amount) { money += amount; }

    public void loseMoney(double amount) { money -= amount; }


    private double strength = 0;

    public double getStrength() { return strength; }

    public void strengthen(double amount) { strength += amount; }

    public void weaken(double amount) { strength -= amount; }


    /** Calculate the cap for the many stats whose caps increase when strength increases.
     */
    public double getStrengthDependentStatCap() { return strength+1000; }



    public Player(int x, int y, int radius, Color color) {
        Player.instance = this;

        String label = "Player";
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }


    /** Gets the speed at which the player is currently intended to move at.
     *
     * @return An int representing number of pixels player will move
     * in a direction per frame if appropriate key is pressed.
     */
    public int getSpeed() {
        int speed = 10;
        if (KeyboardControls.getSprintToggled()) {
            speed *= 2;
        }
        return speed;
    }


    private void movementLogic() {

        int speed = getSpeed();

        if(getLeftPressed() && getRightPressed()) {
            if(getLeftReadTime() > getRightReadTime()) x -= speed;
            else x += speed;
        }
        
        else if(getLeftPressed()) x -= speed;
        else if(getRightPressed()) x += speed;

        if(getUpPressed() && getDownPressed()) {
            if(getUpReadTime() > getDownReadTime()) y -= speed;
            else y += speed;
        }

        else if(getUpPressed()) y -= speed;
        else if(getDownPressed()) y += speed;
    }


    private void borderLogic() {
        x = MyMath.clamp(x, -world.getMidWidth() + getRadius(), world.getMidWidth() - getRadius());
        y = MyMath.clamp(y, -world.getMidHeight() + getRadius(), world.getMidHeight() - getRadius());
    }


    public void commandLogic() {
        GameLayout usedGameLayout = EventLoop.getUsedGameLayout();

        if (isTouchingAnything()) {
            for (Structure structure: getTouching()) {
                usedGameLayout.playerTouchInteraction(structure);
                if (MiscControlStuff.getInteractPressed()) {
                    usedGameLayout.playerPressInteraction(structure);
                }
                if (MiscControlStuff.getInteractTapped()) {
                    usedGameLayout.playerTapInteraction(structure);
                }
            }
        }
    }


    public void statLogic() {
        tire(0.1);

        health = MyMath.clamp(health, 0, getStrengthDependentStatCap());
        energy = MyMath.clamp(energy, 0, getStrengthDependentStatCap());
        money = max(money, 0);
        strength = max(strength, 0);
    }


    @Override
    public void setup(JPanel panel) {
        instance = EventLoop.getUsedGameLayout().player;
        world = World.instances.get(0);
    }


    @Override
    public void act() {
        movementLogic();
        borderLogic();
        commandLogic();
        statLogic();
    }


    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(color);
        g2d.fill(getShape());
    }


}
