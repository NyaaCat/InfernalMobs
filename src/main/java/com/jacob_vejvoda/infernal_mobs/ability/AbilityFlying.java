package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.api.InfernalMobSpawnEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class AbilityFlying implements IAbility {
    @Override
    public void onMobSpawn(InfernalMobSpawnEvent ev) {
        LivingEntity infernal = ev.mobEntity;
        LivingEntity bat = (LivingEntity) infernal.getWorld().spawnEntity(infernal.getLocation(), EntityType.BAT);
        if (bat.addPassenger(infernal)) { // success
            bat.setVelocity(new Vector(0, 1, 0));
            bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1), true);
        } else { // failed
            bat.remove();
        }
    }
}
