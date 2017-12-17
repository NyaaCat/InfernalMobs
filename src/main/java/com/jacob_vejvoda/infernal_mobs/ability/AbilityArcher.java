package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class AbilityArcher implements IAbility {
    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        if (Helper.possibility(0.8)) return;
        List<Player> candidates = new ArrayList<>();
        for (Entity e : mobEntity.getNearbyEntities(16, 16,16)) {
            if (!(e instanceof Player)) continue;
            Player p = (Player) e;
            if (p.getGameMode() == GameMode.CREATIVE) return;
            if (mobEntity.hasLineOfSight(p)) candidates.add(p);
        }
        Player victim = Helper.randomItem(candidates);
        if (victim == null) return;
        Vector v = Helper.unitDirectionVector(
                mobEntity.getEyeLocation().toVector(),
                victim.getEyeLocation().toVector());
        for (int i = 0;i<1;i++) {
            mobEntity.launchProjectile(Arrow.class, v);
        }

        // TODO homing
    }
}
