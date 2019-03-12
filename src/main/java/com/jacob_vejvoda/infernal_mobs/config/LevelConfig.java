package com.jacob_vejvoda.infernal_mobs.config;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.api.InfernalSpawnReason;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.util.logging.Level.WARNING;

public class LevelConfig extends FileConfigure {

    @Serializable(name = "level")
    List<Level> levelMap;
    @Serializable(name = "mobConfig")
    List<MobConfig> mobConfig;

    @Override
    protected String getFileName() {
        return "levels.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return InfernalMobs.instance;
    }

    public int getLevel(double distance) {
        List<Level> result = new ArrayList<>();
        if (levelMap.isEmpty()) {
            return 1;
        }
        levelMap.forEach(level -> {
            if (inRange(level, distance)) {
                result.add(level);
            }
        });
        return getLevelByWeight(result);
    }

    private static int getLevelByWeight(List<LevelConfig.Level> possibleLevel) {
        double sum = possibleLevel.stream().mapToDouble(level -> level.spawnWeight).sum();
        double rand = Helper.rand(0, sum);
        int current = 0;
        for (int i = 0; i < possibleLevel.size(); i++) {
            LevelConfig.Level level = possibleLevel.get(i);
            int weight = level.spawnWeight;
            if (rand >= current && rand < current + weight) {
                return level.level;
            } else current += weight;
        }
        return possibleLevel.get(possibleLevel.size() - 1).level;
    }

    private boolean inRange(Level level, double distance) {
        int from = Math.min(level.fromDistance, level.toDistance);
        int to = Math.max(level.fromDistance, level.toDistance);
        return distance > from && distance < to;
    }

    public double getHealth(double baseHealth, int mobLevel, InfernalSpawnReason reason) {
        double result = baseHealth;
        try {
            if (!mobConfig.isEmpty()) {
                List<MobConfig> levelConf = mobConfig.stream().filter(mobConfig1 -> mobConfig1.level == mobLevel).collect(Collectors.toList());
                //todo check level config on load
                if (levelConf.size() == 0) {
                    //warn missing level conf
                }
                if (levelConf.size() > 1) {
                    //warn duplicate conf
                }
                MobConfig conf = levelConf.get(0);
                result = conf.health;
                if (reason.equals(InfernalSpawnReason.MAMA)) {
                    result *= conf.babyMutiplier;
                }
            }
        }catch (Exception e){
            getPlugin().getLogger().log(WARNING, "exception during reading config for level "+ mobLevel, e);
            result = baseHealth;
        }
        return result;
    }

    public static class Level implements ISerializable {
        @Serializable
        public int fromDistance;
        @Serializable
        public int toDistance;
        @Serializable
        public int level = 1;
        @Serializable
        public int spawnWeight;
    }

    private class MobConfig {
        @Serializable
        public int level;
        @Serializable
        public int health;
        @Serializable
        public int exp;
        @Serializable
        public int damage;
        @Serializable(name = "damage_resist")
        public int damageResist;
        @Serializable(name = "baby_multiplier")
        public double babyMutiplier;
    }
}
