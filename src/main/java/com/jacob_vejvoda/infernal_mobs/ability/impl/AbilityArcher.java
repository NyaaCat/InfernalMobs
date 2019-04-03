package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ability.AbilityProjectile;
import org.bukkit.entity.*;

public class AbilityArcher extends AbilityProjectile {

    public AbilityArcher(){
        this.projectileType = Arrow.class;
        this.onPlayerAttackChance = 0.1;
        this.perCycleChance = 0.1;
        this.effectiveRange = 30;
        this.extraProjectileAmount = 10;
        this.extraSpeedShift = 0.1;
        this.mainSpeed = 1;
    }

//    @Override
//    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
//        if (Helper.possibility(0.8)) return;
//        List<Player> candidates = new ArrayList<>();
//        for (Entity e : mobEntity.getNearbyEntities(16, 16, 16)) {
//            if (!(e instanceof Player)) continue;
//            Player p = (Player) e;
//            GameMode gameMode = p.getGameMode();
//            if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) return;
//            if (mobEntity.hasLineOfSight(p)) candidates.add(p);
//        }
//        Player victim = Helper.randomItem(candidates);
//        if (victim == null) return;
//        Vector v = Helper.unitDirectionVector(
//                mobEntity.getEyeLocation().toVector(),
//                victim.getEyeLocation().toVector());
//        for (int i = 0; i < 1; i++) {
//            mobEntity.launchProjectile(Arrow.class, v);
//        }
//
//        // TODO homing
//    }

    @Override
    protected void postLaunch(Projectile projectile, LivingEntity source, Entity target) {

    }


}
