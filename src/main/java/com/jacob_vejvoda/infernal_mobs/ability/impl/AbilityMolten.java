package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** FIRE_RESISTANCE & set fire on attacker/victim */
public class AbilityMolten implements IAbility {
    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        mobEntity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1), true);
    }

    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (isDirectAttack) attacker.setFireTicks(ConfigReader.getMoltenBurnLength() * 20);
    }

    @Override
    public void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        victim.setFireTicks(ConfigReader.getMoltenBurnLength() * 20);
    }
}
