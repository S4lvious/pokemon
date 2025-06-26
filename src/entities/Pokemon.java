package entities;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Pokemon {
    private String name;
    private int level;
    private int maxHp;
    private int currentHp;
    private int attack;
    private int defense;
    private int speed; // Aggiungo speed per il sistema di turni.
    private BufferedImage sprite;

    public Pokemon(String name, int level, int maxHp, int attack, int defense, int speed) {
        this.name = name;
        this.level = level;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed; 
    }


    private void loadSprite() {
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/assets/" + name.toLowerCase() + ".png"));
        } catch (Exception e) {
            System.err.println("Errore nel caricare lo sprite di " + name + ": " + e.getMessage());
            sprite = null; // Imposta a null se non riesce a caricare lo sprite
        }
    }

    public BufferedImage getSprite() {
        if (sprite == null) {
            loadSprite(); // Carica lo sprite se non è già stato caricato
        }
        return sprite;
    }

    public void takeDamage(int damage) {
        currentHp -= damage;
        if (currentHp < 0) currentHp = 0;
    }

    public boolean isFainted() {
        return currentHp <= 0;
    }

    public int calculateDamage(Pokemon enemy) {
        return Math.max(1, this.attack - enemy.defense);
    }

    public void setCurrentHp(int hp) {
    this.currentHp = Math.max(0, Math.min(hp, maxHp));
}


    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getLevel() {
        return level;
    }
}
