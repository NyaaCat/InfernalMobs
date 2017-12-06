package com.jacob_vejvoda.infernal_mobs.loot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;
import java.util.Map;

/** Template of items to be given to players */
public class LootItem {
    public ItemStack item;
    public RangePair damageRange;
    public RangePair amountRange;
    public Map<Enchantment, RangePair> extraEnchants;
    public List<String> commands;

    /**
     * get a randomized item to be given to player
     */
    public ItemStack get() {
        ItemStack ret = item.clone();
        if (damageRange != null) {
            short damageR = (short) damageRange.get();
            if (damageR < 0) damageR = 0;
            if (damageR >= ret.getType().getMaxDurability())
                damageR = (short) (ret.getType().getMaxDurability() - 1);
            ret.setDurability(damageR);
        }
        if (amountRange != null) {
            ret.setAmount(amountRange.get());
        }
        if (extraEnchants != null) {
            if (ret.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) ret.getItemMeta();
                for (Enchantment e : extraEnchants.keySet()) {
                    meta.addStoredEnchant(e, extraEnchants.get(e).get(), true);
                }
                ret.setItemMeta(meta);
            } else {
                for (Enchantment e : extraEnchants.keySet()) {
                    ret.addUnsafeEnchantment(e, extraEnchants.get(e).get());
                }
            }
        }
        return ret;
    }

    public void applyCommands(Player p) {
        if (commands == null) return;
        for (String cmd : commands) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                    ChatColor.translateAlternateColorCodes('&', cmd)
                            .replace("{player}", p.getName()));
        }
    }
}
