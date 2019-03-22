package com.jacob_vejvoda.infernal_mobs.config;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.NmsUtils;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
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

    public CustomMob getIfCustom(LivingEntity event, int level, boolean isAutoSpawn) {
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

    public void addCustomAttr(Mob mob, CustomMob customMob) {
        try {
            Entity entity = InfernalMobs.instance.getServer().getEntity(mob.entityId);
            String nbttags = customMob.nbttags;
            NmsUtils.setEntityTag(entity, nbttags);
            entity.setCustomName(customMob.name);
            mob.level = customMob.spawnLevel;
            mob.abilityList.clear();
            customMob.abilities.forEach(s -> mob.abilityList.add(EnumAbilities.valueOf(s.toUpperCase())));
        }catch (Exception e){
            InfernalMobs.instance.getLogger().log(Level.WARNING, "config error : ", e);
        }
    }


    Random random = new Random();

    private boolean random(int spawnChance) {
        int i = random.nextInt(100);
        return i < spawnChance;
    }

    public static class CustomMob implements ISerializable, Cloneable {
        @Serializable
        String name;
        @Serializable
        String type;
        @Serializable
        List<Object> levels;
        @Serializable
        List<String> abilities = new ArrayList<>();
        @Serializable
        String nbttags = "";
        @Serializable
        int spawnChance = 50;
        @Serializable
        boolean canAutoSpawn = true;

        public int spawnLevel = 1;

        public CustomMob(){}

        public CustomMob(CustomMob customMob) {
            this.name = customMob.name;
            this.type = customMob.type;
            this.levels = customMob.levels;
            this.abilities = customMob.abilities;
            this.nbttags = customMob.nbttags;
            this.spawnChance = customMob.spawnChance;
            this.canAutoSpawn = customMob.canAutoSpawn;
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
