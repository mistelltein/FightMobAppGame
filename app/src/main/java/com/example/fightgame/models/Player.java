package com.example.fightgame.models;

public class Player {
    private int maxHP;
    private int currentHP;
    private int maxMP;
    private int currentMP;

    public Player(int maxHP, int maxMP) {
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.maxMP = maxMP;
        this.currentMP = maxMP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getCurrentMP() {
        return currentMP;
    }

    public int getMaxMP() {
        return maxMP;
    }

    public void setCurrentHP(int hp) {
        if (hp < 0) hp = 0;
        this.currentHP = hp;
    }

    public void setCurrentMP(int mp) {
        if (mp < 0) mp = 0;
        this.currentMP = mp;
    }

    public void takeDamage(int damage) {
        setCurrentHP(currentHP - damage);
    }

    public void useMana(int cost) {
        setCurrentMP(currentMP - cost);
    }

    public void restoreAll() {
        this.currentHP = maxHP;
        this.currentMP = maxMP;
    }
}
