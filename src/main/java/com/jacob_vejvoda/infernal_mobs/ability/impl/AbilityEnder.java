package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

public class AbilityEnder implements IAbility {
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (mobEntity.isInsideVehicle()) return;
        if (!isDirectAttack) {
            if (Helper.possibility(0.5)) return;
        } else if (Helper.possibility(0.8)) return;
        World attackerWorld = attacker.getWorld();
        for (int i = 0; i < 20; i++) {
            double x = attacker.getLocation().getX() + Helper.rand(-5D, 5D);
            double z = attacker.getLocation().getZ() + Helper.rand(-5D, 5D);
            double y = attacker.getLocation().getBlockZ() + Helper.rand(-5D, 5D);
            Location to = new Location(attackerWorld, x, y, z);
            if(!to.getBlock().getType().isSolid()) {
                EntityTeleportEvent event = new EntityTeleportEvent(mobEntity, mobEntity.getLocation(), to);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                mobEntity.teleport(new Location(mobEntity.getWorld(), x, y, z));
            }
        }

    }

    @Override
    public void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (mobEntity.isInsideVehicle() || Helper.possibility(0.5)) return;
        if (!isDirectAttack) {
            EntityTeleportEvent event = new EntityTeleportEvent(mobEntity, mobEntity.getLocation(), victim.getLocation());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            mobEntity.teleport(victim.getLocation());
        }
    }
}
