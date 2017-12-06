package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/** toss players around */
public class AbilityTosser implements IAbility {
    public static final double EFFECTIVE_RANGE_SQUARED = 6.0*6.0;

    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        Location mobLocation = mobEntity.getLocation();
        mobEntity.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(mobLocation) < EFFECTIVE_RANGE_SQUARED)
                .filter(p -> !p.isSneaking())
                .filter(p -> p.getGameMode() != GameMode.CREATIVE)
                .forEach(p -> p.setVelocity(mobLocation.toVector().subtract(p.getLocation().toVector())));
    }
}
