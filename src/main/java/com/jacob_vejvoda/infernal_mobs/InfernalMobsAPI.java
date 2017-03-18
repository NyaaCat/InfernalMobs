package com.jacob_vejvoda.infernal_mobs;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.*;

public final class InfernalMobsAPI {
    private static final Set<String> AVAILABLE_ABILITIES;
    static {
        AVAILABLE_ABILITIES = new HashSet<>();
        AVAILABLE_ABILITIES.add("mama");
        AVAILABLE_ABILITIES.add("molten");
        AVAILABLE_ABILITIES.add("weakness");
        AVAILABLE_ABILITIES.add("vengeance");
        AVAILABLE_ABILITIES.add("webber");
        AVAILABLE_ABILITIES.add("storm");
        AVAILABLE_ABILITIES.add("sprint");
        AVAILABLE_ABILITIES.add("lifesteal");
        AVAILABLE_ABILITIES.add("ghastly");
        AVAILABLE_ABILITIES.add("ender");
        AVAILABLE_ABILITIES.add("cloaked");
        AVAILABLE_ABILITIES.add("berserk");
        AVAILABLE_ABILITIES.add("1up");
        AVAILABLE_ABILITIES.add("sapper");
        AVAILABLE_ABILITIES.add("rust");
        AVAILABLE_ABILITIES.add("bullwark");
        AVAILABLE_ABILITIES.add("quicksand");
        AVAILABLE_ABILITIES.add("thief");
        AVAILABLE_ABILITIES.add("tosser");
        AVAILABLE_ABILITIES.add("withering");
        AVAILABLE_ABILITIES.add("blinding");
        AVAILABLE_ABILITIES.add("armoured");
        AVAILABLE_ABILITIES.add("poisonous");
        AVAILABLE_ABILITIES.add("potions");
        AVAILABLE_ABILITIES.add("explode");
        AVAILABLE_ABILITIES.add("gravity");
        AVAILABLE_ABILITIES.add("archer");
        AVAILABLE_ABILITIES.add("necromancer");
        AVAILABLE_ABILITIES.add("firework");
        AVAILABLE_ABILITIES.add("flying");
        AVAILABLE_ABILITIES.add("mounted");
        AVAILABLE_ABILITIES.add("morph");
        AVAILABLE_ABILITIES.add("ghost");
        AVAILABLE_ABILITIES.add("confusing");

    }

    /**
     * Check if the given entity is an infernal mob
     * null will be returned if the mob is not infernal
     * NOTE: Even a non-null value is returned, the mob may not exists (e.g. in unloaded chunks)
     *       You may want to do extra check with Bukkit.getEntity(entityId)
     * @param entityId the entityUniqueId
     * @return the infernal mob
     */
    public static Mob asInfernalMob(UUID entityId) {
        Mob mob = infernal_mobs.instance.mobManager.mobMap.get(entityId);
        if (mob != null && Bukkit.getEntity(entityId) != null) {
            mob.entity = Bukkit.getEntity(entityId);
        }
        return mob;
    }

    /**
     * Spawn an infernal mob at the given location with given abilities
     * NOTE: invalid abilities will be ignored silently.
     * @param type mob type
     * @param location spawn location
     * @param abilityList abilities
     * @return the spawned mob
     */
    public static Mob spawnInfernalMob(EntityType type, Location location, Collection<String> abilityList) {
        ArrayList<String> list = new ArrayList<>();
        for (String ability : abilityList) {
            if (AVAILABLE_ABILITIES.contains(ability)) {
                list.add(ability);
            }
        }
        return infernal_mobs.instance.mobManager.spawnMob(type, location, new ArrayList<>(list));
    }

    /**
     * All possible abilities, including those disabled in config file.
     * @return ability list
     */
    public static Set<String> getAvailableAbilities() {
        return new HashSet<>(AVAILABLE_ABILITIES);
    }
}
