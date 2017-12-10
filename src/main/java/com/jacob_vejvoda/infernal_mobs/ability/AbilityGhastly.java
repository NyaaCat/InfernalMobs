package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.stream.Collectors;

/** Send fireballs to nearby players */
public class AbilityGhastly implements IAbility {
    public static double EFFECTIVE_RANGE_SQUARED = 400;

    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        if (Helper.possibility(0.9)) return;
        Location mobLoc = mobEntity.getLocation();
        List<Player> players = mobLoc.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(mobLoc) <= EFFECTIVE_RANGE_SQUARED)
                .filter(p -> p.getGameMode() != GameMode.CREATIVE)
                .collect(Collectors.toList());
        Player victim = Helper.randomItem(players);
        if (victim == null) return;
        Fireball f = mobEntity.launchProjectile(Fireball.class,
                victim.getLocation().toVector().clone().subtract(mobLoc.toVector()).normalize());
        Helper.removeEntityLater(f, 30);
        // TODO fireball tracing player
    }

    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (!isDirectAttack) {
            if (Helper.possibility(0.5)) return;
            Fireball f = mobEntity.launchProjectile(Fireball.class,
                    attacker.getLocation().toVector().clone().subtract(mobEntity.getLocation().toVector()).normalize());
            Helper.removeEntityLater(f, 30);
            // TODO fireball tracing player
        }
    }
}
