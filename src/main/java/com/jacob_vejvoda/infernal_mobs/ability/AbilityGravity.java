package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static org.bukkit.Material.AIR;

/** Randomly levitate nearby players */
public class AbilityGravity implements IAbility{
    public static final double EFFECTIVE_RANGE_SQUARED = 100;
    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        if (!Helper.possibility(0.2)) return;
        Location mobLocation = mobEntity.getLocation();
        mobLocation.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(mobLocation) <= EFFECTIVE_RANGE_SQUARED)
                .filter(p -> p.getGameMode() != GameMode.CREATIVE)
                .filter(p -> {
                    Location t = p.getLocation().clone();
                    t.add(0,-2,0);
                    Block b = p.getWorld().getBlockAt(t);
                    return b != null && b.getType() != AIR;
                })
                .filter(p -> Helper.possibility(0.2))
                .forEach(p -> levitate(p, ConfigReader.getGravityLevitateLength()));
    }

    // TODO replace by levitate potion effect
    private static void levitate(final Entity e, final int time) {
        boolean needCheckFlight = e instanceof Player;
        boolean couldFly = needCheckFlight && ((Player)e).getAllowFlight();
        if (needCheckFlight) ((Player)e).setAllowFlight(true);

        new BukkitRunnable() {
            private int counter = 0;
            @Override
            public void run() {
                if (counter < 0) return;
                counter++;
                if (counter <= 40) {
                    e.setVelocity(e.getVelocity().add(new Vector(0, 0.1, 0)));
                } else if (counter < 40 + time*20) {
                    final Vector vec = e.getVelocity();
                    vec.setY(0.01);
                    e.setVelocity(vec);
                } else {
                    counter = -1;
                    cancel();
                    if (needCheckFlight && !couldFly) ((Player)e).setAllowFlight(false);
                }
            }
        }.runTaskTimer(InfernalMobs.instance, 1,1);
    }
}
