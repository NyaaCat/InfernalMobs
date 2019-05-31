package com.jacob_vejvoda.infernal_mobs.loot;

import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class LootManager {
    public static final Random rnd = new Random();

    private final InfernalMobs plugin;
    public LootConfig cfg;

    public LootManager(InfernalMobs plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        if (new File(plugin.getDataFolder(), "loot_v2.yml").isFile()) { // loot file exists
            cfg = LootConfig.parse(new File(plugin.getDataFolder(), "loot_v2.yml"));
        } else { // no config file
            cfg = new LootConfig();
            save();
        }
    }

    public ItemStack getLootByName(Player p, String name) {
        if (!cfg.lootItems.containsKey(name)) return null;
        cfg.lootItems.get(name).applyCommands(p);
        return cfg.lootItems.get(name).get();
    }

    /**
     * Give random loot, NOTE: commands will be applied here.
     *
     * @param player commands to be applied on
     * @param powers mob power level
     * @return the loot, or null
     */
    public ItemStack getRandomLoot(final Player player, final int powers) {
        LootItem loot = cfg.getRandomDrop(powers);
        if (loot == null) return new ItemStack(Material.AIR);
        if (player != null) loot.applyCommands(player);
        return loot.get();
    }

    public void save() {
        cfg.dump(new File(plugin.getDataFolder(), "loot_v2.yml"));
    }

    public boolean hasLootForName(String name) {
        return cfg.lootItems.containsKey(name);
    }
}
