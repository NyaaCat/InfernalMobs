package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AbilityWithering implements IAbility {
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 140, 1), true);
    }

    @Override
    public void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 140, 1), true);
    }
}
