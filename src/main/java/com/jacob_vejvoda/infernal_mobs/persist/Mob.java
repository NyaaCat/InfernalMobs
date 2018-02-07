package com.jacob_vejvoda.infernal_mobs.persist;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;

import java.util.List;
import java.util.UUID;

public class Mob {
    //public LivingEntity entity;
    public UUID entityId;
    public int lives;
    public ParticleEffect particleEffect;
    public List<EnumAbilities> abilityList;
    public int maxMamaInfernal = 0;

    public Mob(UUID entityId, int lives, ParticleEffect particleEffect, List<EnumAbilities> abilityList) {
        this.entityId = entityId;
        this.lives = lives;
        this.particleEffect = particleEffect;
        this.abilityList = abilityList;
        if (abilityList.contains(EnumAbilities.MAMA)) {
            this.maxMamaInfernal = Helper.rand(1, abilityList.size());
        }
    }

    public int getMobLevel() {
        return abilityList.size();
    }
}
