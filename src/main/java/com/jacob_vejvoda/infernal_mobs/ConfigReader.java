package com.jacob_vejvoda.infernal_mobs;

import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.config.LevelConfig;
import com.jacob_vejvoda.infernal_mobs.persist.ParticleEffect;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class ConfigReader {
    private static LevelConfig levelConfig;

    private static ConfigurationSection cfg() {
        return InfernalMobs.instance.getConfig();
    }
    
    public static ParticleEffect getRandomParticleEffect() {
        List<ParticleEffect> candidates = new ArrayList<>();
        for (String s : cfg().getStringList("mobParticles")) {
            ParticleEffect e = ParticleEffect.parse(s);
            candidates.add(e);
        }
        if (candidates.size() <= 0) return new ParticleEffect(Particle.LAVA, 1, 10);
        return Helper.randomItem(candidates);
    }

    public static List<EntityType> getDisabledBabyNameList() {
        List<EntityType> ret = new ArrayList<>();
        for (String str : cfg().getStringList("disabledBabyMobs")) {
            try {
                EntityType type = EntityType.valueOf(str);
                if (!type.isAlive() || !type.isSpawnable())
                    throw new RuntimeException();
                ret.add(type);
            } catch (Exception ex) {
                InfernalMobs.instance.getLogger().warning(str + " is not a valid mob type");
            }
        }
        return ret;
    }

    public static boolean particlesEnabled() {
        return cfg().getBoolean("enableParticles");
    }

    public static int getGravityLevitateLength() {
        int amount = 6;
        if (cfg().getString("gravityLevitateLength") != null) {
            amount = cfg().getInt("gravityLevitateLength");
        }
        return amount;
    }

    public static boolean isEnabledWorld(World w) {
        String n = w.getName();
        List<String> enabledWorlds = cfg().getStringList("enabledworlds");
        return enabledWorlds.contains("<all>") || enabledWorlds.contains(n);
    }

    public static boolean isEnabledMobType(EntityType t) {
        return cfg().getList("enabledmobs").contains(t.name());
    }

    public static List<EntityType> getEnabledEntityTypes() {
        List<EntityType> ret = new ArrayList<>();
        for (String str : cfg().getStringList("enabledmobs")) {
            try {
                EntityType type = EntityType.valueOf(str);
                if (!type.isAlive() || !type.isSpawnable())
                    throw new RuntimeException();
                ret.add(type);
            } catch (Exception ex) {
                InfernalMobs.instance.getLogger().warning(str + " is not a valid infernal mob type");
            }
        }
        return ret;
    }

    public static int getNaturalSpawnMinHeight() {
        return cfg().getInt("naturalSpawnHeight");
    }

    public static List<SpawnReason> getEnabledSpawnReasons() {
        List<SpawnReason> ret = new ArrayList<>();
        for (String str : cfg().getStringList("enabledSpawnReasons")) {
            try {
                SpawnReason reason = SpawnReason.valueOf(str);
                ret.add(reason);
            } catch (Exception ex) {
                InfernalMobs.instance.getLogger().warning(str + " is not a valid spawn reason");
            }
        }
        return ret;
    }

    public static boolean isHealthByPower() {
        return cfg().getBoolean("healthByPower");
    }

    public static boolean isHealthByDistance() {
        return cfg().getBoolean("healthByDistance");
    }

    public static double getDistancePerHealthLevel() {
        return cfg().getInt("addDistance", 100);
    }

    public static double getHealthPerHealthLevel() {
        return cfg().getInt("healthToAdd", 100);
    }

    public static int getHealthMultiplier() {
        return cfg().getInt("healthMultiplier", 1);
    }

    public static boolean isInfernalMobHasNameTag() {
        return cfg().getInt("nameTagsLevel", 0) > 0;
    }

    public static boolean isInfernalMobNameTagAlwaysVisible() {
        return cfg().getInt("nameTagsLevel", 0) == 2;
    }
    
    public static String getMobNameTag() {
        return cfg().getString("nameTagsName", "&fInfernal <mobName>");
    }

    public static String getNameTagPrefixByLevel(int level) {
        if (cfg().isString("levelPrefixs." + level)) {
            return cfg().getString("levelPrefixs." + level);
        } else {
            return getNamePrefix();
        }
    }

    public static String getNamePrefix() {
        return cfg().getString("namePrefix", "");
    }

    public static boolean isScoreboardEnabled() {
        return cfg().getBoolean("enableScoreBoard", false);
    }

    public static boolean isKillMountee() {
        return "death".equalsIgnoreCase(cfg().getString("mountFate", ""));
    }

    public static boolean isRemovalMountee() {
        return "removal".equalsIgnoreCase(cfg().getString("mountFate", ""));
    }

    public static boolean isEnabledRider(EntityType type) {
        return cfg().getStringList("enabledRiders").contains(type.name());
    }

    public static List<EntityType> getMounteeTypes() {
        List<EntityType> ret = new ArrayList<>();
        for (String str : cfg().getStringList("enabledMounts")) {
            try {
                EntityType type = EntityType.valueOf(str);
                if (!type.isAlive() || !type.isSpawnable() || type == EntityType.BAT)
                    throw new RuntimeException();
                ret.add(type);
            } catch (Exception ex) {
                InfernalMobs.instance.getLogger().warning(str + " is not a valid mountee mob type");
            }
        }
        return ret;
    }

    public static boolean isHorseMounteeNeedSaddle() {
        return cfg().getBoolean("horseMountsHaveSaddles", true);
    }

    /** Only for horse as mountee */
    public static boolean isArmouredMounteeNeedArmour() {
        return cfg().getBoolean("armouredMountsHaveArmour", true);
    }

    /** how much percent should infernal spawn, return floating point num from 0~1 */
    public static double getInfernalNaturalSpawningPercentage() {
        return ((double)cfg().getInt("chance")) / 100D;
    }

    public static boolean isSpawnedLevelByDistance() {
        return cfg().getBoolean("powerByDistance", false);
    }

    public static double getSpawnDistancePerLevel() {
        return cfg().getInt("addDistance", 100);
    }

    public static int getMinimalLevel() {
        return cfg().getInt("minpowers");
    }

    public static int getMaximumLevel() {
        return cfg().getInt("maxpowers");
    }

    public static List<EnumAbilities> getEnabledAbilities() {
        List<EnumAbilities> ret = new ArrayList<>();
        for (EnumAbilities ab : EnumAbilities.values()) {
            if (cfg().getBoolean(ab.name().toLowerCase())) {
                ret.add(ab);
            }
        }
        return ret;
    }

    public static boolean isSpwanMessageEnabled() {
        return cfg().getBoolean("enableSpawnMessages", false);
    }

    public static List<String> getSpwanMessages() {
        return cfg().getStringList("spawnMessages");
    }

    public static boolean isBroadcastSpawnMessageServer() {
        return cfg().getInt("spawnMessageRadius", 0) == -2;
    }

    public static boolean isBroadcastSpawnMessageWorld() {
        return cfg().getInt("spawnMessageRadius", 0) == -1;
    }

    public static int getSpawnMessageBroadcaseRadius() {
        return cfg().getInt("spawnMessageRadius", 0);
    }

    public static boolean isDropEnabled() {
        return cfg().getBoolean("enableDrops");
    }

    public static boolean isFarmingDropEnabled() {
        return cfg().getBoolean("enableFarmingDrops");
    }

    public static boolean isCreativeDropEnabled() {
        return !cfg().getBoolean("noCreativeDrops", false);
    }

    public static int getDropChance() {
        return cfg().getInt("dropChance");
    }

    public static int getXpMultiplier() {
        return cfg().getInt("xpMultiplier");
    }

    public static Color getFireworkColor() {
        return Color.fromRGB(
                cfg().getInt("fireworkColour.red", 255),
                cfg().getInt("fireworkColour.green", 0),
                cfg().getInt("fireworkColour.blue", 0)
        );
    }

    public static int getMoltenBurnLength() {
        return cfg().getInt("moltenBurnLength", 5);
    }

    // number of hearts
    public static int getVengeanceDamage() {
        return cfg().getInt("vengeanceDamage", 3);
    }

    // number of hearts
    public static int getBerserkDamage() {
        return cfg().getInt("berserkDamage", 3);
    }

    public static int getMamaSpawnAmount() {
        return cfg().getInt("mamaSpawnAmount", 3);
    }

    public static boolean isMobDeathMessageEnabled() {
        return cfg().getBoolean("enableDeathMessages", true);
    }

    public static boolean isDeathMessageBroadcastAllWorld() {
        return cfg().getBoolean("broadcastToAllWorld", false);
    }

    public static boolean isBossbarEnabled(){return cfg().getBoolean("bossBarEnabled", true);}

    public static boolean isEnhanceEnabled() {
        return cfg().getBoolean("configEnhance", false);
    }

    public static LevelConfig getLevelConfig() {
        return levelConfig == null? loadLevelConfig(): levelConfig;
    }

    private static LevelConfig loadLevelConfig() {
        levelConfig = new LevelConfig();
        levelConfig.load();
        return levelConfig;
    }

}