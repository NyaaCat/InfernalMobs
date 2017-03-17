package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.enchantments.Enchantment;

class LevelledEnchantment {
    public Enchantment getEnchantment;
    public int getLevel;

    LevelledEnchantment(final Enchantment enchantment, final int level) {
        this.getEnchantment = enchantment;
        this.getLevel = level;
    }
}
