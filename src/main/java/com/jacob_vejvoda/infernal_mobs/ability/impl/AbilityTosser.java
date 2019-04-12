package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/** toss players around */
public class AbilityTosser implements IAbility {
    public static final double EFFECTIVE_RANGE_SQUARED = 6.0*6.0;

    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        if (Helper.possibility(0.9)) return;
        Location mobLocation = mobEntity.getLocation();
        mobEntity.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(mobLocation) < EFFECTIVE_RANGE_SQUARED)
                .filter(p -> p.getLocation().distanceSquared(mobLocation) > 4)
                .filter(p -> !p.isSneaking())
                .filter(p -> Helper.validGamemode(p))
                .forEach(p -> {
                    Vector v = mobEntity.getEyeLocation().toVector().clone().subtract(p.getLocation().toVector());
                    double len = v.length();
                    if (len > 6 || len < 1) return;
                    v.normalize().multiply(Math.min(2D, len/2D));
                    p.setVelocity(v);
                });
    }
}
