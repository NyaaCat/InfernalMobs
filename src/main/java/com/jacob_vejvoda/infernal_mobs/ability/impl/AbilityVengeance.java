package com.jacob_vejvoda.infernal_mobs.ability.impl;

import cat.nyaa.nyaacore.Pair;
import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AbilityVengeance implements IAbility {
    final List<Pair<Player, Mob>> cdList = new ArrayList<>();
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (ev.getCause().equals(EntityDamageEvent.DamageCause.THORNS))return;
        final Pair<Player, Mob> playerMobPair = new Pair<>(attacker, mob);
        if (cdList.contains(playerMobPair)){
            return;
        }
        if (Helper.possibility(0.5)) {
//            attacker.damage(ConfigReader.getVengeanceDamage() * 2);
            double vDamage = ev.getFinalDamage() * (((double) ConfigReader.getVengeanceDamage()) / 100d);
            attacker.damage(Math.min(vDamage, ConfigReader.getLevelConfig().getDamage(vDamage, mob.getMobLevel())), mobEntity);
            cdList.add(playerMobPair);
            new BukkitRunnable(){
                @Override
                public void run() {
                    cdList.remove(playerMobPair);
                }
            }.runTaskLater(InfernalMobs.instance, 20);
        }
    }
}
