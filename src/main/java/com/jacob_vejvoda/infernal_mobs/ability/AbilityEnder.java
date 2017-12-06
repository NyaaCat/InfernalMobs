package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AbilityEnder implements IAbility {
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (mobEntity.isInsideVehicle()) return;
        if (!isDirectAttack) {
            mobEntity.teleport(attacker.getLocation());
        } else if (Helper.possibility(0.2)) {
            double x = mobEntity.getLocation().getX() + Helper.rand(-5D, 5D);
            double z = mobEntity.getLocation().getZ() + Helper.rand(-5D, 5D);
            double y = mobEntity.getWorld().getHighestBlockYAt((int)x, (int)z) + 1;
            mobEntity.teleport(new Location(mobEntity.getWorld(), x,y,z));
        }
    }

    @Override
    public void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (mobEntity.isInsideVehicle()) return;
        if (!isDirectAttack) mobEntity.teleport(victim.getLocation());
    }
}
