package com.jacob_vejvoda.infernal_mobs.config;

import cat.nyaa.nyaacore.configuration.ISerializable;

import java.util.Map;

public class SpawnConfig implements ISerializable{
//    private Map<String, Object> spawnConfig;

    @Serializable
    int level;
    @Serializable
    Attr attr;
    @Serializable(name = "spawn")
    Range range;

    public SpawnConfig(){

    }

    SpawnConfig(Map<String, Object> v) {
//        this.spawnConfig = v;
//        attr = new Attr((Map<String, Object>) spawnConfig.get("attr"));
//        range = new Range((Map<String, Object>) spawnConfig.get("spawn"));
    }


    public int getSpawnWeight() {
        return range.weight;
    }

    public int getLevel() {
//        return ((Number) spawnConfig.get("level")).intValue();r
        return level;
    }

    public int getTo() {
        return range.to;
    }

    public int getFrom() {
        return range.from;
    }

    public int getExp() {
        return attr.exp;
    }

    public int getHealth() {
        return attr.health;
    }

    public double getDamageResist() {
        return attr.damageResist;
    }

    public double getBabyMultiplier() {
        return attr.babyMultiplier;
    }

    public double getDamage() {
        return attr.damage;
    }

    public static class Range implements ISerializable{
        @Serializable
        int from;
        @Serializable
        int to;
        @Serializable
        int weight;

        public Range(){

        }

        Range(Map<String, Object> spawn) {
            from = ((Number) spawn.get("from")).intValue();
            from = ((Number) spawn.get("to")).intValue();
            from = ((Number) spawn.get("weight")).intValue();
        }
    }

    public static class Attr implements ISerializable {
        @Serializable
        int health;
        @Serializable
        int damage;
        @Serializable
        int damageResist;
        @Serializable
        int exp;
        @Serializable(name = "baby")
        double babyMultiplier = 1.0;
        public Attr(){

        }

        public Attr(Map<String, Object> attr) {
            health = ((Number) attr.get("health")).intValue();
            damage = ((Number) attr.get("damage")).intValue();
            damageResist = ((Number) attr.get("damageResist")).intValue();
            exp = ((Number) attr.get("exp")).intValue();
            babyMultiplier = ((Number) attr.get("baby")).doubleValue();
        }
    }
}