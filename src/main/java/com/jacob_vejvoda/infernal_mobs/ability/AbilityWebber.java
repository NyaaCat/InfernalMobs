package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class AbilityWebber implements IAbility {
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (Helper.possibility(0.7)) return;
        boolean webJail = false;
        if (mobEntity instanceof Spider && Helper.possibility(0.1)) webJail = true;
        Location loc = attacker.getLocation();
        if (!webJail) {
            setWebWithDecay(loc.getBlock(), 60, false);
        } else {
            for (Block b : Helper.getSphere(loc.getBlock(), 4)) {
                setWebWithDecay(b, 30, true);
            }
        }
    }

    @Override
    public void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (Helper.possibility(0.7)) return;
        boolean webJail = false;
        if (mobEntity instanceof Spider && Helper.possibility(0.1)) webJail = true;
        Location loc = victim.getLocation();
        if (!webJail) {
            setWebWithDecay(loc.getBlock(), 60, false);
        } else {
            for (Block b : Helper.getSphere(loc.getBlock(), 4)) {
                setWebWithDecay(b, 30, true);
            }
        }
    }

    private static void setWebWithDecay(Block b, int seconds, boolean allowInAir){
        if (b == null || b.getType() != Material.AIR) return;
        if (!allowInAir) {
            Block below_block = b.getRelative(BlockFace.DOWN);
            if (!below_block.getType().isSolid()) return;
        }
        b.setType(Material.WEB);
        (new BukkitRunnable(){
            @Override
            public void run() {
                if (b.getType() == Material.WEB) {
                    b.setType(Material.AIR);
                }
            }
        }).runTaskLater(InfernalMobs.instance, seconds * 20);
    }
}
