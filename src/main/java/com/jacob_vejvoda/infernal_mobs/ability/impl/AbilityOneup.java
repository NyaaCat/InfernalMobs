package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.api.InfernalSpawnReason;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

/** SECOND LIFE */
// TODO
public class AbilityOneup implements IAbility {
    @Override
    public void onDeath(LivingEntity mobEntity, Mob mob, Player killer, EntityDeathEvent ev) {
        if (mob.lives > 1) {
            Mob m = InfernalMobs.instance.mobManager
                    .spawnMob(mobEntity.getType(), mobEntity.getLocation(), mob.abilityList, mobEntity.getUniqueId(), InfernalSpawnReason.ONEUP);
            m.lives = mob.lives - 1;
        }
    }
}
