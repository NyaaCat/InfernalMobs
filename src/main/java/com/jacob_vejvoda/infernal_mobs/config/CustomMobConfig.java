package com.jacob_vejvoda.infernal_mobs.config;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.NmsUtils;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.MobManager;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.api.InfernalSpawnReason;
import com.jacob_vejvoda.infernal_mobs.loot.legacy.LegacyLootConfig;
import com.jacob_vejvoda.infernal_mobs.loot.legacy.LegacyLootManager;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

public class CustomMobConfig extends FileConfigure {

    @Serializable
    Map<String, CustomMob> mobs = new HashMap<>();

    public CustomMobConfig() {
    }

    @Override
    protected String getFileName() {
        return "mobs.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return InfernalMobs.instance;
    }

    public CustomMob determineCustom(LivingEntity event, int level, boolean isAutoSpawn) {
        EntityType type = event.getType();
        if (mobs.isEmpty()) return null;
        for (Map.Entry<String, CustomMob> entry : mobs.entrySet()) {
            CustomMob customMob = entry.getValue();
            if (isAutoSpawn && !customMob.canAutoSpawn) continue;
            if (type.equals(EntityType.valueOf(customMob.type.toUpperCase()))) {
                boolean valid = customMob.inLevelRange(level) && (!isAutoSpawn || random(customMob.spawnChance));
                if (valid) {
                    CustomMob mob = new CustomMob(customMob);
                    mob.spawnLevel = level;
                    return mob;
                }
            }
        }
        return null;
    }

    public void setAbilities(List<EnumAbilities> abilityList, CustomMob customMob) {
        abilityList.clear();
        customMob.abilities.forEach(s -> abilityList.add(EnumAbilities.valueOf(s.toUpperCase())));
    }

    public void addCustomAttr(Mob mob, CustomMob customMob) {
        try {
            Entity entity = InfernalMobs.instance.getServer().getEntity(mob.entityId);
            String nbttags = customMob.nbttags;
            NmsUtils.setEntityTag(entity, nbttags);
            entity.setCustomName(customMob.name);
            mob.level = customMob.spawnLevel;
            mob.isCustomMob = true;
            mob.customLoot = getLoot(customMob);
        } catch (Exception e) {
            InfernalMobs.instance.getLogger().log(Level.WARNING, "config error : ", e);
        }
    }

    private String getLoot(CustomMob customMob) {
        List<String> loots = customMob.loots;
        if (loots == null || loots.isEmpty()) return null;
        LegacyLootManager lootManager = InfernalMobs.instance.lootManager;
        Map<String, Double> randomMap = new LinkedHashMap<>();
        loots.forEach(s -> {
            try {
                String[] split = s.split(":");
                String lootName = split[0];
                if (!lootManager.hasLootForName(lootName)) throw new IllegalArgumentException();
                double weight = Double.parseDouble(split[1]);
                randomMap.put(lootName, weight);
            } catch (Exception e) {
                InfernalMobs.instance.getLogger().log(Level.WARNING, "loot \"" + s + "\" for custom mob \"" + customMob.name + "\" is not valid.");
            }
        });
        return LegacyLootConfig.weightedRandom(randomMap);
    }


    Random random = new Random();

    private boolean random(int spawnChance) {
        int i = random.nextInt(100);
        return i < spawnChance;
    }

    public CustomMob getByName(String mobName) {
        return mobs.get(mobName);
    }

    public Mob spawnCustomMob(MobManager mobManager, Location location, List<EnumAbilities> abilities, CustomMob cm) {
        return mobManager.spawnMob(EntityType.valueOf(cm.type.toUpperCase()), location, abilities, InfernalSpawnReason.COMMAND);
    }

    public Map<String, CustomMob> getCustomMobs() {
        return mobs;
    }

    public static class CustomMob implements ISerializable, Cloneable {
        @Serializable
        public String name;
        @Serializable
        public String type;
        @Serializable
        public List<Object> levels;
        @Serializable
        public List<String> abilities = new ArrayList<>();
        @Serializable
        public String nbttags = "";
        @Serializable
        public int spawnChance = 50;
        @Serializable
        public boolean canAutoSpawn = true;
        @Serializable
        public List<String> loots;
        @Serializable
        public int smSpawnLevel = -1;

        public int spawnLevel = 1;

        public CustomMob() {
        }

        public CustomMob(CustomMob customMob) {
            this.name = customMob.name;
            this.type = customMob.type;
            this.levels = customMob.levels;
            this.abilities = customMob.abilities;
            this.nbttags = customMob.nbttags;
            this.spawnChance = customMob.spawnChance;
            this.canAutoSpawn = customMob.canAutoSpawn;
            this.loots = customMob.loots;
        }

        boolean inLevelRange(int level) {
            try {
                boolean result = false;
                for (Object o :
                        levels) {
                    if (o instanceof String) {
                        String str = ((String) o).replaceAll(" ", "");
                        String[] split = str.split("-");
                        int from = Integer.parseInt(split[0]);
                        int to = Integer.parseInt(split[1]);
                        result = level >= from && level <= to;
                    } else if (o instanceof Integer) {
                        Integer o1 = (Integer) o;
                        result = o1.equals(level);
                    } else {
                        throw new RuntimeException();
                    }
                    if (result) break;
                }
                return result;
            } catch (Exception e) {
                InfernalMobs.instance.getLogger().log(Level.SEVERE, "wrong level config \"" + name + "\"");
            }
            return false;
        }
    }
}
