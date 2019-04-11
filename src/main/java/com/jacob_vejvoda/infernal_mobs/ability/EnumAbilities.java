package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.ability.impl.*;
import com.jacob_vejvoda.infernal_mobs.ability.impl.extended.AbilityMeteor;
import com.jacob_vejvoda.infernal_mobs.ability.impl.extended.AbilityUltraStrike;
import com.jacob_vejvoda.infernal_mobs.api.InfernalMobSpawnEvent;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public enum EnumAbilities implements IAbility {
    MAMA("mama", AbilityMama.class),
    MOLTEN("molten", AbilityMolten.class),
    WEAKNESS("weakness", AbilityWeakness.class),
    VENGEANCE("vengeance", AbilityVengeance.class),
    WEBBER("webber", AbilityWebber.class),
    STORM("storm", AbilityStorm.class),
    SPRINT("sprint", AbilitySprint.class),
    LIFESTEAL("lifesteal", AbilityLifesteal.class),
    GHASTLY("ghastly", AbilityGhastly.class),
    ENDER("ender", AbilityEnder.class),
    CLOAKED("cloaked", AbilityCloaked.class),
    BERSERK("berserk", AbilityBerserk.class),
    ONEUP("1up", AbilityOneup.class),
    SAPPER("sapper", AbilitySapper.class),
    RUST("rust", AbilityRust.class),
    BULLWARK("bullwark", AbilityBullwark.class),
    QUICKSAND("quicksand", AbilityQuicksand.class),
    TOSSER("tosser", AbilityTosser.class),
    WITHERING("withering", AbilityWithering.class),
    BLINDING("blinding", AbilityBlinding.class),
    ARMOURED("armoured", AbilityArmoured.class),
    POISONOUS("poisonous", AbilityPoisonous.class),
    POTIONS("potions", AbilityPotions.class),
    EXPLODE("explode", AbilityExplode.class),
    GRAVITY("gravity", AbilityGravity.class),
    ARCHER("archer", AbilityArcher.class),
    NECROMANCER("necromancer", AbilityNecromancer.class),
    FIREWORK("firework", AbilityFirework.class),
    FLYING("flying", AbilityFlying.class),
    MOUNTED("mounted", AbilityMounted.class),
    MORPH("morph", AbilityMorph.class),
    GHOST("ghost", AbilityGhost.class),
    CONFUSING("confusing", AbilityConfusing.class),
    METEOR("meteor", AbilityMeteor.class),
    ULTRASTRIKE("ultrastrike", AbilityUltraStrike.class);

    private final IAbility instance;

    EnumAbilities(String name, Class<? extends IAbility> cls) {
        IAbility t;
        try {
            t = cls.getConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        t.readExtra(this.name());
        instance = t;
    }

    public static void reloadAbility(){
        for (EnumAbilities value : values()) {
            value.readExtra(value.name());
        }
    }


    @Override
    public void perCycleEffect(LivingEntity mobEntity, Mob mob) {
        instance.perCycleEffect(mobEntity, mob);
    }

    @Override
    public void onMobSpawn(InfernalMobSpawnEvent ev) {
        instance.onMobSpawn(ev);
    }

    @Override
    public void onDeath(LivingEntity mobEntity, Mob mob, Player killer, EntityDeathEvent ev) {
        instance.onDeath(mobEntity, mob, killer, ev);
    }

    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        instance.onPlayerAttack(mobEntity, mob, attacker, isDirectAttack, ev);
    }

    @Override
    public void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        instance.onAttackPlayer(mobEntity, mob, victim, isDirectAttack, ev);
    }
}
