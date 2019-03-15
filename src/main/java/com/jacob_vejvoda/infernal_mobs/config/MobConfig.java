package com.jacob_vejvoda.infernal_mobs.config;

import cat.nyaa.nyaacore.configuration.ISerializable;

import java.util.LinkedHashMap;

class MobConfig implements ISerializable {
    @Serializable
    public int level;
    @Serializable
    public int health;
    @Serializable
    public int exp;
    @Serializable
    public int damage;
    @Serializable(name = "damage_resist")
    public double damageResist;
    @Serializable(name = "baby_multiplier")
    public double babyMultiplier;

    public MobConfig(Object level, Object health, Object exp, Object damage, Object damageResist, Object babyMultiplier) {
        this.level = (int) level;
        this.health = (int) health;
        this.exp = (int) exp;
        this.damage = (int) damage;
        this.damageResist = ((Number) damageResist).doubleValue();
        this.babyMultiplier = ((Number) babyMultiplier).doubleValue();
    }

    public static MobConfig createFrom(LinkedHashMap<String, Object> m) {
        return new MobConfig(m.get("level"), m.get("health"), m.get("exp"), m.get("damage"), m.get("damageResist"), m.get("babyMultiplier"));
    }
}