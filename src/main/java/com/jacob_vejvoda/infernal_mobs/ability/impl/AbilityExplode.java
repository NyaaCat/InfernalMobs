package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public class AbilityExplode implements IAbility {
    @Override
    public void onDeath(LivingEntity mobEntity, Mob mob, Player killer, EntityDeathEvent ev) {
        Location loc = mobEntity.getLocation();
        loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4, false, false);
    }
}
