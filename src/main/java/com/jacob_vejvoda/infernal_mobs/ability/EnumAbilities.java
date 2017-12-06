package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.ability.*;

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
    THIEF("thief", AbilityThief.class),
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
    CONFUSING("confusing", AbilityConfusing.class);

    private final IAbility instance;

    EnumAbilities(String name, Class<? extends IAbility> cls) {
        IAbility t;
        try {
            t = cls.getConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        instance = t;
    }

    // TODO create stub methods, dispatching to instance
}
