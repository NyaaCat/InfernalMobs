package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AbilityVengeance implements IAbility {
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (Helper.possibility(0.5)) attacker.damage(ConfigReader.getVengeanceDamage() * 2);
    }
}
