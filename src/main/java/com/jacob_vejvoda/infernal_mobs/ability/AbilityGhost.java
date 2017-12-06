package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public class AbilityGhost implements IAbility {
    @Override
    public void onDeath(LivingEntity mobEntity, Mob mob, Player killer, EntityDeathEvent ev) {
        // TODO this.plugin.mobManager.spawnGhost(event.getEntity().getLocation());
    }
}
