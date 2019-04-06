package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public abstract class AbilityProjectile implements IAbility {
    private static final Vector x_axis = new Vector(1, 0, 0);
    private static final Vector y_axis = new Vector(0, 1, 0);
    private static final Vector z_axis = new Vector(0, 0, 1);

    protected int effectiveRange = 30;
    protected int extraProjectileAmount = 0;
    protected double perCycleChance = 0.5;
    protected double onPlayerAttackChance = 1;
    protected double mainSpeed = 1;
    protected double extraSpeedShift = -0.5;

    protected Class<? extends Projectile> projectileType = Arrow.class;

    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        if (!Helper.possibility(perCycleChance)) return;
        List<Player> nearbyPlayers = mobEntity.getNearbyEntities(effectiveRange, effectiveRange, effectiveRange).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> ((Player) entity))
                .filter(this::isGamemodeValid)
                .collect(Collectors.toList());
        if (!nearbyPlayers.isEmpty()) {
            nearbyPlayers.forEach(player -> {
                if (!mobEntity.hasLineOfSight(player)) return;
                Vector vector = Helper.unitDirectionVector(mobEntity.getEyeLocation().toVector(), player.getEyeLocation().toVector())
                        .multiply(mainSpeed);
                Projectile pro = launch(mobEntity, player,vector, false);
                postLaunch(pro,mobEntity ,player);
                launchExtraProjectiles(vector, mobEntity, player);
            });
        }
    }

    protected Projectile launch(LivingEntity mobEntity, Entity target, Vector vector, boolean isExtra) {
        return mobEntity.launchProjectile(projectileType, vector);
    }

    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (!Helper.possibility(onPlayerAttackChance)) return;
        double speed = mainSpeed + extraSpeedShift;
        if (speed <= 0.1) speed = 0.1;
        Vector vector = Helper.unitDirectionVector(mobEntity.getEyeLocation().toVector(), attacker.getEyeLocation().toVector())
                .multiply(speed);
        Projectile pro = launch(mobEntity, attacker, vector, false);
        postLaunch(pro,mobEntity ,attacker );
        launchExtraProjectiles(vector, mobEntity, attacker);
    }

    private boolean isGamemodeValid(Player entity) {
        GameMode gameMode = entity.getGameMode();
        return !gameMode.equals(GameMode.CREATIVE) && !gameMode.equals(GameMode.SPECTATOR);
    }

    protected abstract void postLaunch(Projectile projectile, LivingEntity source, Entity target);

    protected void launchExtraProjectiles(Vector direction, LivingEntity source, Entity target) {
        if (this.extraProjectileAmount == 0) return;
        int range = 180;
        range = Math.abs(range) % 360;
        double phi = range / 180f * Math.PI;//弧度
        Vector a, b;
        Vector ax1 = direction.getCrossProduct(z_axis);
        if (ax1.length() < 0.01) {
            a = x_axis.clone();
            b = y_axis.clone();
        } else {
            a = ax1.normalize();
            b = direction.getCrossProduct(a).normalize();
        }
        for (int i = 0; i < this.extraProjectileAmount; i++) {
            double z = range == 0 ? 1 : ThreadLocalRandom.current().nextDouble(Math.cos(phi), 1);
            double det = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
            double theta = Math.acos(z);
            // theta 是偏移角， delta是旋转角
            // ((a cosδ)+(b sinδ))·sinθ + direction · cosθ
            Vector v = a.clone().multiply(Math.cos(det)).add(b.clone().multiply(Math.sin(det))).multiply(Math.sin(theta)).add(direction.clone().multiply(Math.cos(theta)));
            Projectile launch = launch(source, target, v, true);
            if (launch == null) return;
            Helper.removeEntityLater(launch, 30);
        }
    }
}
