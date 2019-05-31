package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class AbilityFirework implements IAbility {
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (!isDirectAttack) {
            Firework fw = mobEntity.getWorld().spawn(mobEntity.getEyeLocation(), Firework.class, AbilityFirework::setupFirework);
            fw.detonate();
        }
    }

    public static void setupFirework(Firework fw) {
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(ConfigReader.getFireworkColor()).with(FireworkEffect.Type.BALL_LARGE).build());
        meta.setPower(0);
        fw.setFireworkMeta(meta);
        fw.setVelocity(new Vector(0,0,0));
    }
}
