package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Run fast */
public class AbilitySprint implements IAbility{
    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        mobEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1), true);
    }
}
