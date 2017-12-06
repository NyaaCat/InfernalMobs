package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AbilityStorm implements IAbility {
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (!isDirectAttack && Helper.possibility(0.1)) {
            Location loc = attacker.getLocation().clone();
            int y = loc.getWorld().getHighestBlockYAt(loc) + 1;
            loc.setY(y);
            loc.getWorld().strikeLightning(loc);
        }
    }

    @Override
    public void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (!isDirectAttack && Helper.possibility(0.1)) {
            Location loc = victim.getLocation().clone();
            int y = loc.getWorld().getHighestBlockYAt(loc) + 1;
            loc.setY(y);
            loc.getWorld().strikeLightning(loc);
        }
    }
}
