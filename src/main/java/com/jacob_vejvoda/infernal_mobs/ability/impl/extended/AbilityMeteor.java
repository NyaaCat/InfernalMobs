package com.jacob_vejvoda.infernal_mobs.ability.impl.extended;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.ability.AbilityProjectile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Queue;

public class AbilityMeteor extends AbilityProjectile {
    Queue<LaunchTask> standby = new LinkedList<>();

    public AbilityMeteor() {
        this.effectiveRange = 25;
        this.extraProjectileAmount = 5;
        this.onPlayerAttackChance = 0.1;
        this.perCycleChance = 0.3;
        this.projectileType = Fireball.class;
        this.mainSpeed = 4;
        this.extraSpeedShift = -1;
    }

    @Override
    protected void postLaunch(Projectile projectile, LivingEntity source, Entity target) {
        Bukkit.getScheduler().runTaskLater(InfernalMobs.instance, () -> {
            if (!standby.isEmpty()) {
                LaunchTask poll;
                while ((poll = standby.poll()) != null) {
                    Vector vector = Helper.unitDirectionVector(poll.projectileSource.getLocation().toVector(), poll.victim.getLocation().toVector());
                    launchExtraProjectiles(vector, poll.projectileSource, target);
                }
            }
        }, 10);
        Helper.removeEntityLater(projectile, 30);
    }

    @Override
    protected Projectile launch(LivingEntity mobEntity, Entity target, Vector vector, boolean isExtra) {
        if (!isExtra) {
            Location location = target.getLocation().clone();
            location.add(0, 10, 0);
            if (isClearSky(location)) {
                ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, (e) -> {
                    e.setVisible(false);
                    e.setPersistent(false);
                    e.setCanPickupItems(false);
                    e.setGlowing(true);
                    e.setBasePlate(false);
                    e.setArms(false);
                    e.setMarker(true);
                    e.setInvulnerable(true);
                    e.setGravity(false);
                });
                LaunchTask task = new LaunchTask(mobEntity, armorStand, target);
                standby.offer(task);
                Vector fallVector = Helper.unitDirectionVector(armorStand.getLocation().toVector(), target.getLocation().toVector());
                if (armorStand.hasLineOfSight(target)) {
                    Projectile projectile = armorStand.launchProjectile(this.projectileType, fallVector);
                    projectile.setShooter(mobEntity);
                    projectile.setGravity(true);
                    Helper.removeEntityLater(projectile, 40);
                    return projectile;
                }else {
                    return null;
                }
            }else {
                return null;
            }
        }else {
            return mobEntity.launchProjectile(this.projectileType, vector);
        }
    }

    private boolean isClearSky(Location location) {
        Location from = location.clone().subtract(5, 0, 5);
        Location to = location.clone().add(5, 0, 5);
        World world = location.getWorld();
        while (!from.equals(to)) {
            if (!world.getBlockAt(from).getType().equals(Material.AIR))
                return false;
            if (!world.getBlockAt(to).getType().equals(Material.AIR))
                return false;
            from.add(1, 0, 1);
            to.subtract(1, 0, 1);
        }
        return true;
    }

    private class LaunchTask {
        LivingEntity projectileSource;
        LivingEntity attacker;
        Entity victim;

        public LaunchTask(LivingEntity mobEntity, ArmorStand armorStand, Entity target) {
            this.projectileSource = armorStand;
            this.attacker = mobEntity;
            this.victim = target;
        }
    }
}
