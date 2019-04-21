package com.jacob_vejvoda.infernal_mobs.persist;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class Mob {
    //public LivingEntity entity;
    public UUID entityId;
    public int lives;
    public ParticleEffect particleEffect;
    public int level;
    public List<EnumAbilities> abilityList;
    public int maxMamaInfernal = 0;
    public boolean isCustomMob = false;
    public String customLoot;
    public EntityDamageByEntityEvent lastDamageCause;

    public Mob(UUID entityId, int lives, ParticleEffect particleEffect, int level, List<EnumAbilities> abilityList){
        this.entityId = entityId;
        this.lives = lives;
        this.particleEffect = particleEffect;
        this.abilityList = abilityList;
        this.level = level;
        if (abilityList.contains(EnumAbilities.MAMA)) {
            this.maxMamaInfernal = Helper.rand(1, abilityList.size());
        }
    }

    public Mob(UUID entityId, int lives, ParticleEffect particleEffect, List<EnumAbilities> abilityList) {
        this(entityId, lives, particleEffect, abilityList.size(), abilityList);
    }

    public int getMobLevel() {
        return level;
    }
}
