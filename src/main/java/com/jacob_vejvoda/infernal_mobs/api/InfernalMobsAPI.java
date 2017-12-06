package com.jacob_vejvoda.infernal_mobs.api;


import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.*;

public final class InfernalMobsAPI {
    /**
     * Check if the given entity is an infernal mob
     * null will be returned if the mob is not infernal
     * NOTE: Even a non-null value is returned, the mob may not exists (e.g. in unloaded chunks)
     * You may want to do extra check with Bukkit.getEntity(entityId)
     *
     * @param entityId the entityUniqueId
     * @return the infernal mob
     */
    public static Mob asInfernalMob(UUID entityId) {
        return InfernalMobs.instance.mobManager.mobMap.get(entityId);
    }

    /**
     * Spawn an infernal mob at the given location with given abilities
     * NOTE: invalid abilities will be ignored silently.
     *
     * @param type        mob type
     * @param location    spawn location
     * @param abilityList abilities
     * @return the spawned mob
     */
    public static Mob spawnInfernalMob(EntityType type, Location location, Collection<EnumAbilities> abilityList) {
        ArrayList<EnumAbilities> list = new ArrayList<>();
        list.addAll(abilityList);
        return InfernalMobs.instance.mobManager.spawnMob(type, location, list, InfernalSpawnReason.API);
    }
}
