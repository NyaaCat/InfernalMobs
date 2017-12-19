package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.api.InfernalMobSpawnEvent;
import com.jacob_vejvoda.infernal_mobs.api.InfernalSpawnReason;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class AbilityMounted implements IAbility {
    @Override
    public void onMobSpawn(InfernalMobSpawnEvent ev) {
        if (!ConfigReader.isEnabledRider(ev.mobEntity.getType()) && ev.reason != InfernalSpawnReason.COMMAND) return;
        EntityType mounteeType = Helper.randomItem(ConfigReader.getMounteeTypes());
        LivingEntity mounteeEntity = (LivingEntity) ev.mobEntity.getWorld().spawn(ev.mobEntity.getLocation(), mounteeType.getEntityClass());
        InfernalMobs.instance.mobManager.unnaturallySpawned.put(mounteeEntity.getUniqueId(), true);
        mounteeEntity.addPassenger(ev.mobEntity);
        if (mounteeEntity instanceof Horse) {
            ((Horse) mounteeEntity).setColor(Helper.randomItem(Arrays.asList(Horse.Color.values())));
            ((Horse) mounteeEntity).setStyle(Helper.randomItem(Arrays.asList(Horse.Style.values())));
            ((Horse) mounteeEntity).setTamed(true);
            if (ConfigReader.isHorseMounteeNeedSaddle()) {
                ((Horse) mounteeEntity).getInventory().setSaddle(new ItemStack(Material.SADDLE));
            }
            if (ev.mob.abilityList.contains(EnumAbilities.ARMOURED) && ConfigReader.isArmouredMounteeNeedArmour()) {
                ((Horse) mounteeEntity).getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING));
            }
        }
    }
}
