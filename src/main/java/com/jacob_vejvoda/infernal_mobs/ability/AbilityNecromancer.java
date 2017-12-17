package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.stream.Collectors;

/** Randomly send wither skulls to nearby players */
public class AbilityNecromancer implements IAbility {
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
        WitherSkull w = mobEntity.launchProjectile(WitherSkull.class,
                Helper.unitDirectionVector(
                        mobEntity.getEyeLocation().toVector(),
                        victim.getEyeLocation().toVector()));
        Helper.removeEntityLater(w, 30);
        // TODO WitherSkull tracing player
    }

    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (isDirectAttack) {
            if (Helper.possibility(0.5)) return;
            WitherSkull w = mobEntity.launchProjectile(WitherSkull.class,
                    Helper.unitDirectionVector(
                            mobEntity.getEyeLocation().toVector(),
                            attacker.getEyeLocation().toVector()));
            Helper.removeEntityLater(w, 30);
            // TODO WitherSkull tracing player
        }
    }
}
