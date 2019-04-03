package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.ability.AbilityProjectile;
import org.bukkit.entity.*;

/** Send fireballs to nearby players */
public class AbilityGhastly extends AbilityProjectile {
//    public static double EFFECTIVE_RANGE_SQUARED = 400;

    public AbilityGhastly(){
        this.projectileType = Fireball.class;
        this.perCycleChance = 0.1;
        this.extraProjectileAmount = 3;
        this.mainSpeed = 1;
        this.extraSpeedShift = -0.5;
        this.effectiveRange = 30;
        this.onPlayerAttackChance = 0.1;
    }

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
//        Fireball f = mobEntity.launchProjectile(Fireball.class,
//                Helper.unitDirectionVector(
//                        mobEntity.getEyeLocation().toVector(),
//                        victim.getEyeLocation().toVector()));
//        Helper.removeEntityLater(f, 30);
//        // TODO fireball tracing player
//    }


//    @Override
//    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
//        if (!isDirectAttack) {
//            if (Helper.possibility(0.5)) return;
//            Fireball f = mobEntity.launchProjectile(Fireball.class,
//                    Helper.unitDirectionVector(
//                            mobEntity.getEyeLocation().toVector(),
//                            attacker.getEyeLocation().toVector()));
//            Helper.removeEntityLater(f, 30);
//            // TODO fireball tracing player
//        }
//    }

    @Override
    protected void postLaunch(Projectile projectile, LivingEntity source, Entity target) {
        Helper.removeEntityLater(projectile, 30);
    }
}
