package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Invisible mobs */
public class AbilityCloaked implements IAbility {
    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        mobEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 400, 1), true);
    }
}
