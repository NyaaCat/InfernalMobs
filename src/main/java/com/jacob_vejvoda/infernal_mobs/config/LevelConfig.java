package com.jacob_vejvoda.infernal_mobs.config;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.api.InfernalSpawnReason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

import static java.util.logging.Level.WARNING;

public class LevelConfig extends FileConfigure {

    @Serializable(name = "level")
    Map<String, SpawnConfig> spawnConfig;

    @StandaloneConfig
    AbilityConfig abilityConfig = new AbilityConfig();

    @Override
    protected String getFileName() {
        return "levels.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return InfernalMobs.instance;
    }

    public int getLevel(double distance) {
        try {
            List<SpawnConfig> result = new ArrayList<>();
            if (spawnConfig.isEmpty()) {
                return 1;
            }
            spawnConfig.forEach((k, v) -> {
                if (inRange(v, distance)) {
                    result.add(v);
                }
            });
            if (!result.isEmpty()) {
                return getLevelByWeight(result);
            } else return -1;
        } catch (Exception e) {
            getPlugin().getLogger().log(WARNING, "exception during calculating distance", e);
            return 1;
        }
    }

    private static int getLevelByWeight(List<SpawnConfig> possibleLevel) {
        double sum = possibleLevel.stream().mapToDouble(level -> level.getSpawnWeight()).sum();
        double rand = Helper.rand(0, sum);
        int current = 0;
        for (int i = 0; i < possibleLevel.size(); i++) {
            SpawnConfig level = possibleLevel.get(i);
            int weight = level.getSpawnWeight();
            if (rand >= current && rand < current + weight) {
                return level.getLevel();
            } else current += weight;
        }
        return possibleLevel.get(possibleLevel.size() - 1).getLevel();
    }

    public double getHealth(double baseHealth, int mobLevel, InfernalSpawnReason reason) {
        double result = baseHealth;
        try {
            SpawnConfig levelConf = getLevelConf(mobLevel);
            //todo check level config on load
            if (levelConf != null) {
                result = levelConf.getHealth();
                if (reason.equals(InfernalSpawnReason.MAMA)) {
                    result *= levelConf.getBabyMultiplier();
                }
            }
        } catch (Exception e) {
            getPlugin().getLogger().log(WARNING, "exception during reading config for level " + mobLevel, e);
            result = baseHealth;
        }
        return result;
    }

    private boolean inRange(SpawnConfig level, double distance) {
        int from = Math.min(level.getFrom(), level.getTo());
        int to = Math.max(level.getFrom(), level.getTo());
        return distance > from && distance < to;
    }

    public int getExp(int baseXp, int mobLevel) {
        int xp = baseXp;
        try {
            SpawnConfig levelConf = getLevelConf(mobLevel);
            xp = levelConf.getExp();
        } catch (Exception e) {
            getPlugin().getLogger().log(WARNING, "exception during reading config for level " + mobLevel, e);
            xp = baseXp;
        }
        return xp;
    }

    public double getDamage(double originalDamage, int mobLevel) {
        double damage = originalDamage;
        try {
            for (SpawnConfig cfg : spawnConfig.values()) {
                if (cfg.getLevel() == mobLevel) {
                    damage = cfg.getDamage();
                    break;
                }
            }
        } catch (Exception e) {
            getPlugin().getLogger().log(WARNING, "exception during reading config for level " + mobLevel, e);
            damage = originalDamage;
        }
        return damage;
    }

    public double calcResistedDamage(double originDamage, int mobLevel) {
        double damage = originDamage;
        try {
            SpawnConfig levelConf = getLevelConf(mobLevel);
            if (levelConf != null) {
                double damageResist = levelConf.getDamageResist();
                if (damageResist >= 100) {
                    damage = 0;
                } else {
                    damage = (1 - (damageResist / 100d)) * damage;
                }
            }
        } catch (Exception e) {
            getPlugin().getLogger().log(WARNING, "exception during reading config for level " + mobLevel, e);
            damage = originDamage;
        }
        return damage;
    }

    private SpawnConfig getLevelConf(int mobLevel) {
        for (Map.Entry<String, SpawnConfig> entry : spawnConfig.entrySet()) {
//            Object v = entry.getValue();
//            SpawnConfig cfg = new SpawnConfig((Map<String, Object>) v);
            SpawnConfig cfg = entry.getValue();
            if (cfg.getLevel() == mobLevel) {
                return cfg;
            }
        }
        return null;
    }

    public boolean isInRange(Location location) {
        double distance = location.getWorld().getSpawnLocation().distance(location);
        boolean inRange = false;
        for (SpawnConfig sc :
                spawnConfig.values()) {
            if (inRange(sc, distance)){
                inRange = true;
                break;
            }
        }
         return inRange;
    }

    public List<EnumAbilities> getAbilitiyList(int level) {
        List<EnumAbilities> result = abilityConfig.getConfigFor(level);
        if (result.isEmpty()){
            getPlugin().getLogger().log(WARNING, "no ability for level "+level);
        }
        return result;
    }
}
