package com.jacob_vejvoda.infernal_mobs.ability.impl.extended;

import cat.nyaa.nyaacore.configuration.ISerializable;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.ability.Property;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class AbilityUltraStrike implements IAbility {
    @Property
    public double possibility = 0.1;

    @Property
    public int amount = 5;

    @Property
    public ParticleSetting particle = new ParticleSetting();

    @Property
    public int explodeRange = 3;

    @Property
    public int nearbyRange = 30;

    @Property
    public double damage = 20;

    @Property
    public int delay = 60;

    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        if (!Helper.possibility(possibility)) return;
        List<Entity> nearbyEntities = mobEntity.getNearbyEntities(nearbyRange, nearbyRange, nearbyRange)
                .stream().filter(entity -> entity instanceof Player)
                .collect(Collectors.toList());
        Collections.shuffle(nearbyEntities);
        Queue<Entity> queue = new LinkedList<>(nearbyEntities);

        for (int i = 0; i < amount; i++) {
            Entity poll = queue.poll();
            if (poll != null) {
                Location location = poll.getLocation();
                if (!mobEntity.hasLineOfSight(poll)) {
                    i--;
                    continue;
                }
                summonUltraStrike(location, mobEntity);
            } else {
                int x = Helper.rand(-nearbyRange, nearbyRange);
                int z = Helper.rand(-nearbyRange, nearbyRange);
                Location location = mobEntity.getLocation().add(new Vector(x, 0, z));
                int originalLocationY = location.getBlockY();
                findOpenArea(location);
                if (location.getBlockY() - originalLocationY< 10) {
                    summonUltraStrike(location, mobEntity);
                } else {
                    i--;
                }
            }
        }
    }

    private void findOpenArea(Location location) {
        while (location.getBlockY() < 255) {
            Block block = location.getBlock();
            if (!block.getType().equals(Material.AIR)) {
                location.add(0, 1, 0);
                continue;
            } else if (block.getRelative(BlockFace.DOWN).getType().equals(Material.AIR)) {
                location.add(0, -1, 0);
                continue;
            } else if (!block.getRelative(BlockFace.UP).getType().equals(Material.AIR)) {
                location.add(0, 2, 0);
                continue;
            }
            break;
        }
    }

    private void summonUltraStrike(Location location, LivingEntity mobEntity) {
        try {
            double x = particle.delta.get(0);
            double y = particle.delta.get(1);
            double z = particle.delta.get(2);
            location.getWorld().spawnParticle(Particle.valueOf(particle.name), location, particle.amount, x, y, z, particle.speed);
            for (int i = 0; i < delay / 20 ; i++) {
                Bukkit.getScheduler().runTaskLater(InfernalMobs.instance, ()->{
                    location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                }, i*20);
            }
            Bukkit.getScheduler().runTaskLater(InfernalMobs.instance, () -> {
                boom(location, mobEntity);
            }, delay);
        } catch (Exception e) {
            InfernalMobs.instance.getLogger().log(Level.WARNING, "config error");
        }
    }

    private void boom(Location location, LivingEntity mobEntity) {
        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, explodeRange, explodeRange, explodeRange);
        location.getWorld().playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
        location.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, location, 1);
        if (!nearbyEntities.isEmpty()) {
            nearbyEntities.stream().filter(entity -> entity instanceof LivingEntity)
                    .map(entity -> ((LivingEntity) entity))
                    .forEach(livingEntity -> {
                        if (!livingEntity.equals(mobEntity))
                            livingEntity.damage(damage,mobEntity);
                    });
        }
    }

    public static class ParticleSetting implements ISerializable {
        @Serializable
        String name = "FIREWORKS_SPARK";
        @Serializable
        List<Double> delta = Arrays.asList(0d, 15d, 0d);
        @Serializable
        double speed = 0;
        @Serializable
        int amount = 500;
    }
}
