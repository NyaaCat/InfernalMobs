package com.jacob_vejvoda.infernal_mobs.ability.impl.extended;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.ability.AbilityProjectile;
import com.jacob_vejvoda.infernal_mobs.config.AbilityConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import javax.swing.text.Style;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

public class AbilityMeteor extends AbilityProjectile {
    Queue<LaunchTask> standby = new LinkedList<>();

    public AbilityMeteor() {

    }

    @Override
    protected void postLaunch(Projectile projectile, LivingEntity source, Entity target) {
        Bukkit.getScheduler().runTaskLater(InfernalMobs.instance, () -> {
            if (!standby.isEmpty()) {
                LaunchTask poll;
                while ((poll = standby.poll()) != null) {
                    Vector vector = Helper.unitDirectionVector(poll.projectileSource.getLocation().toVector(), poll.victim.getLocation().toVector());
                    vector.multiply(mainSpeed + extraSpeedShift);
                    launchExtraProjectiles(vector, poll.projectileSource, target);
                }
            }
        }, 10);
        if (projectile != null) {
            Helper.removeEntityLater(projectile, 50);
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
                    e.setVisible(false);
                    e.setPersistent(false);
                    e.setCanPickupItems(false);
                    e.setGlowing(false);
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
                    Projectile projectile = armorStand.launchProjectile(this.projectileType, fallVector);
                    projectile.setVelocity(fallVector);
//                    projectile.setDirection(fallVector);
                    projectile.setShooter(mobEntity);
                    projectile.setGravity(false);
//                    Helper.removeEntityLater(projectile, 40);
                    return projectile;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            if (!(mobEntity instanceof ArmorStand)) return null;
            Projectile projectile = mobEntity.launchProjectile(this.projectileType, vector);
            projectile.setVelocity(vector);
            projectile.setShooter(mobEntity);
            projectile.setGravity(false);
            return projectile;
        }
    }

    @Override
    public void readExtra(String string) {
        this.effectiveRange = 25;
        this.extraProjectileAmount = 5;
        this.onPlayerAttackChance = 0.5;
        this.perCycleChance = 0.1;
        this.projectileType = Fireball.class;
        this.mainSpeed = 0.01;
        this.extraSpeedShift = -0.005;

        AbilityConfig abilityConfig = ConfigReader.getAbilityConfig();
        AbilityConfig.Attr meteor = abilityConfig.getAttrForAbility("meteor");
        if (!meteor.hasExtra()) {
            meteor.putExtra("effectiveRange", effectiveRange);
            meteor.putExtra("extraProjectileAmount", extraProjectileAmount);
            meteor.putExtra("onPlayerAttackChance", onPlayerAttackChance);
            meteor.putExtra("perCycleChance", perCycleChance);
            meteor.putExtra("projectileType", projectileType.getName());
            meteor.putExtra("mainSpeed", mainSpeed);
            meteor.putExtra("extraSpeedShift", extraSpeedShift);
            abilityConfig.save();
        }
        try {
            this.projectileType = (Class<? extends Projectile>) Class.forName(meteor.getStringExtra("projectileType"));
            this.effectiveRange = meteor.getIntExtra("effectiveRange");
            this.extraProjectileAmount = meteor.getIntExtra("extraProjectileAmount");
            this.onPlayerAttackChance = meteor.getDoubleExtra("onPlayerAttackChance");
            this.perCycleChance = meteor.getDoubleExtra("perCycleChance");
            this.mainSpeed = meteor.getDoubleExtra("mainSpeed");
            this.extraSpeedShift = meteor.getDoubleExtra("extraSpeedShift");
        } catch (ClassCastException | ClassNotFoundException e) {
            InfernalMobs.instance.getLogger().log(Level.WARNING, "failed to load extra setting", e.getMessage());
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
