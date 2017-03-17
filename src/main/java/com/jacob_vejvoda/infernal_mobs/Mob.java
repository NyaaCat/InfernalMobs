package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.UUID;

class Mob {
    public Entity entity;
    public UUID id;
    public World world;
    public boolean infernal;
    public int lives;
    public String effect;
    public ArrayList<String> abilityList;

    Mob(final Entity type, final UUID i, final World w, final boolean in, final ArrayList<String> l, final int li, final String e) {
        this.abilityList = new ArrayList<String>();
        this.entity = type;
        this.id = i;
        this.world = w;
        this.infernal = in;
        this.abilityList = l;
        this.lives = li;
        this.effect = e;
    }

    @Override
    public String toString() {
        return "Name: " + this.entity.getType().getName() + " Infernal: " + this.infernal + "Abilities:" + this.abilityList;
    }

    public void setLives(final int i) {
        this.lives = i;
    }
}
