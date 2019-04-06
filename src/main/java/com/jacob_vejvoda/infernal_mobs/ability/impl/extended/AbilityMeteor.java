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
        this.mainSpeed = 0.0001;
        this.extraSpeedShift = -0.00005;
    }

    @Override
    protected void postLaunch(Projectile projectile, LivingEntity source, Entity target) {
        Bukkit.getScheduler().runTaskLater(InfernalMobs.instance, () -> {
            if (!standby.isEmpty()) {
                LaunchTask poll;
                while ((poll = standby.poll()) != null) {
                    Vector vector = Helper.unitDirectionVector(poll.projectileSource.getLocation().toVector(), poll.victim.getLocation().toVector());
                    vector.multiply(mainSpeed+extraSpeedShift);
                    launchExtraProjectiles(vector, poll.projectileSource, target);
                }
            }
        }, 10);
        if (projectile !=null){
            Helper.removeEntityLater(projectile, 30);
        }
    }

    @Override
    protected Projectile launch(LivingEntity mobEntity, Entity target, Vector vector, boolean isExtra) {
        if (!isExtra) {
            Location location = target.getLocation().clone();
            location.add(0, 30, 0);
            location.setPitch(90);
            if (isClearSky(location)) {
                ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, (e) -> {
                    e.setVisible(true);
                    e.setPersistent(false);
                    e.setCanPickupItems(false);
                    e.setGlowing(true);
                    e.setBasePlate(false);
                    e.setArms(false);
                    e.setMarker(true);
                    e.setInvulnerable(true);
                    e.setGravity(false);
                    e.setCollidable(false);
                });
                Helper.removeEntityLater(armorStand, 100);
                LaunchTask task = new LaunchTask(mobEntity, armorStand, target);
                standby.offer(task);
                Vector fallVector = Helper.unitDirectionVector(armorStand.getLocation().toVector(), target.getLocation().toVector());
                fallVector.multiply(mainSpeed);
                if (armorStand.hasLineOfSight(target)) {
                    Fireball projectile = armorStand.launchProjectile(Fireball.class, fallVector);
                    projectile.setVelocity(fallVector);
                    projectile.setDirection(fallVector);
                    projectile.setShooter(mobEntity);
                    projectile.setGravity(false);
//                    Helper.removeEntityLater(projectile, 40);
                    return projectile;
                }else {
                    return null;
                }
            }else {
                return null;
            }
        }else {
            if (!(mobEntity instanceof ArmorStand)) return null;
            Fireball projectile = (Fireball) mobEntity.launchProjectile(this.projectileType, vector);
            projectile.setVelocity(vector);
            projectile.setDirection(vector);
            projectile.setShooter(mobEntity);
            projectile.setGravity(false);
            return projectile;
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
