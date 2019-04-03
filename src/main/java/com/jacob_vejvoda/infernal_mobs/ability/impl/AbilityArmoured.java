package com.jacob_vejvoda.infernal_mobs.ability.impl;

import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.ability.IAbility;
import com.jacob_vejvoda.infernal_mobs.api.InfernalMobSpawnEvent;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** DAMAGE_RESISTANCE effect for mobs */
public class AbilityArmoured implements IAbility {
    @Override
    public void perCycleEffect(LivingEntity e, Mob mob) {
        if (e instanceof Skeleton || e instanceof Zombie) return;
        e.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1), true);
    }

    @Override
    public void onMobSpawn(InfernalMobSpawnEvent ev) {
        Mob mob = ev.mob;
        LivingEntity mobEntity = ev.mobEntity;
        mobEntity.setCanPickupItems(false);
        EntityEquipment ee = mobEntity.getEquipment();

        boolean isCloaked = mob.abilityList.contains(EnumAbilities.CLOAKED);

        // helmet & chestplate required for all mobs
        ee.setHelmetDropChance(0.0f);
        ee.setChestplateDropChance(0.0f);
        ee.setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
        ee.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));

        // leggings & boots for all visible mobs
        ee.setLeggingsDropChance(0.0f);
        ee.setBootsDropChance(0.0f);
        ee.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
        ee.setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));

        // skeletons use bow
        if (mobEntity.getType() == EntityType.SKELETON) {
            ee.setItemInMainHand(new ItemStack(Material.BOW, 1));
            ee.setItemInMainHandDropChance(0);
        } else { // else use sword
            ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
            sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
            ee.setItemInMainHand(sword);
            ee.setItemInMainHandDropChance(0);
        }
    }
}
