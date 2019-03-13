package com.jacob_vejvoda.infernal_mobs.config;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.api.InfernalSpawnReason;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
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

    public double getHealth(double baseHealth, int mobLevel, InfernalSpawnReason reason) {
        double result = baseHealth;
        try {
            if (!mobConfig.isEmpty()) {
                MobConfig levelConf = getLevelConf(mobLevel);
                //todo check level config on load
                if (levelConf != null) {
                    result = levelConf.health;
                    if (reason.equals(InfernalSpawnReason.MAMA)) {
                        result *= levelConf.babyMutiplier;
                    }
                }
            }
        } catch (Exception e) {
            getPlugin().getLogger().log(WARNING, "exception during reading config for level " + mobLevel, e);
            result = baseHealth;
        }
        return result;
    }

    private boolean inRange(Level level, double distance) {
        int from = Math.min(level.fromDistance, level.toDistance);
        int to = Math.max(level.fromDistance, level.toDistance);
        return distance > from && distance < to;
    }

    private MobConfig getLevelConf(int mobLevel) {
        List<MobConfig> levelConf = mobConfig.stream().filter(mobConfig1 -> mobConfig1.level == mobLevel).limit(1).collect(Collectors.toList());
        return levelConf.get(0);
    }

    public int getExp(int baseXp, int mobLevel) {
        int xp = baseXp;
        MobConfig mobConfig1 = getLevelConf(mobLevel);
        if (mobConfig1 != null && mobConfig1.level == mobLevel) {
            xp = mobConfig1.exp;
        }

        return xp;
    }

    public double getDamage(double originalDamage, int mobLevel) {
        double damage = originalDamage;
        for (MobConfig mc :
                mobConfig) {
            if (mc.level == mobLevel) {
                damage = mc.damage;
                break;
            }
        }
        return damage;
    }

    public double calcResistedDamage(double originDamage, int mobLevel) {
        double damage = originDamage;
        MobConfig levelConf = getLevelConf(mobLevel);
        if (levelConf != null) {
            double damageResist = levelConf.damageResist;
            if (damageResist >= 100) {
                damage = 0;
            } else {
                damage = ((damageResist / 100d) + 1d) * damage;
            }
        }
        return damage;
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
        public double damageResist;
        @Serializable(name = "baby_multiplier")
        public double babyMutiplier;
    }
}
