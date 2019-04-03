package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static org.bukkit.Material.AIR;

/** Randomly levitate nearby players */
public class AbilityGravity implements IAbility {
    public static final double EFFECTIVE_RANGE_SQUARED = 25;
    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        if (!Helper.possibility(0.2)) return;
        Location mobLocation = mobEntity.getLocation();
        mobLocation.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(mobLocation) <= EFFECTIVE_RANGE_SQUARED)
                .filter(p -> p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR)
                .filter(p -> {
                    Location t = p.getLocation().clone();
                    t.add(0,-2,0);
                    Block b = p.getWorld().getBlockAt(t);
                    return b != null && b.getType() != AIR;
                })
                .filter(p -> Helper.possibility(0.2))
                .forEach(p -> levitate(p, ConfigReader.getGravityLevitateLength()));
    }

    private static void levitate(Player p, int time) {
        if (p.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, time * 20, 1));
    }
}
