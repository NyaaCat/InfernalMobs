package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.ability.AbilityProjectile;
import org.bukkit.entity.*;

/** Randomly send wither skulls to nearby players */
public class AbilityNecromancer extends AbilityProjectile {
//    public static double EFFECTIVE_RANGE_SQUARED = 400;
//
//    @Override
//    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
//        if (Helper.possibility(0.9)) return;
//        Location mobLoc = mobEntity.getLocation();
//        List<Player> players = mobLoc.getWorld().getPlayers().stream()
//                .filter(p -> p.getLocation().distanceSquared(mobLoc) <= EFFECTIVE_RANGE_SQUARED)
//                .filter(p -> p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR)
//                .collect(Collectors.toList());
//        Player victim = Helper.randomItem(players);
//        if (victim == null) return;
//        WitherSkull w = mobEntity.launchProjectile(WitherSkull.class,
//                Helper.unitDirectionVector(
//                        mobEntity.getEyeLocation().toVector(),
//                        victim.getEyeLocation().toVector()));
//        Helper.removeEntityLater(w, 30);
//        // TODO WitherSkull tracing player
//    }
//
//    @Override
//    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
//        if (isDirectAttack) {
//            if (Helper.possibility(0.5)) return;
//            WitherSkull w = mobEntity.launchProjectile(WitherSkull.class,
//                    Helper.unitDirectionVector(
//                            mobEntity.getEyeLocation().toVector(),
//                            attacker.getEyeLocation().toVector()));
//            Helper.removeEntityLater(w, 30);
//            // TODO WitherSkull tracing player
//        }
//    }


    public AbilityNecromancer() {
        this.projectileType = WitherSkull.class;
        this.extraProjectileAmount = 0;
        this.mainSpeed = 1;
        this.extraSpeedShift = -0.5;
        this.onPlayerAttackChance = 0.1;
        this.perCycleChance = 0.1;
    }

    @Override
    protected void postLaunch(Projectile projectile, LivingEntity source, Entity target) {
        Helper.removeEntityLater(projectile, 30);
    }
}
