package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AbilityLifesteal implements IAbility {
    @Override
    public void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        double damage = ev.getFinalDamage();
        double newHealth = mobEntity.getHealth() + damage;
        double maxHealth = mobEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (newHealth > maxHealth) newHealth = maxHealth;
        mobEntity.setHealth(newHealth);
    }
}
