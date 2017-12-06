package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.api.InfernalMobSpawnEvent;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public interface IAbility {
    default void perCycleEffect(LivingEntity mobEntity, Mob mob) {}
    default void onMobSpawn(InfernalMobSpawnEvent ev) {}
    default void onDeath(LivingEntity mobEntity, Mob mob, Player killer, EntityDeathEvent ev) {}
    default void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {}
    default void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {}
}
