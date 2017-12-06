package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

public class AbilityFirework implements IAbility {
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (!isDirectAttack) {
            mobEntity.getWorld().spawn(mobEntity.getEyeLocation(), Firework.class, AbilityFirework::setupFirework);
        }
    }

    public static void setupFirework(Firework fw) {
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(ConfigReader.getFireworkColor()).with(FireworkEffect.Type.BALL_LARGE).build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
        fw.setVelocity(new Vector(0,1,0));
    }
}
