package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AbilityMama implements IAbility {
    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (Helper.possibility(0.9)) return;
        EntityType type = mobEntity.getType();
        int amount = ConfigReader.getMamaSpawnAmount();
        for (int i = 0;i < amount;i++) {
            Entity child = mobEntity.getWorld().spawnEntity(mobEntity.getLocation(), type);
            if (child instanceof Zombie)
                ((Zombie) child).setBaby(true);
            if (child instanceof Ageable)
                ((Ageable) child).setBaby();
            InfernalMobs.instance.mobManager.mamaSpawned.put(child.getUniqueId(), mobEntity.getUniqueId());
        }
    }
}
