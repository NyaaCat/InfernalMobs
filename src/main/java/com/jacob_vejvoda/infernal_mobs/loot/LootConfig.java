package com.jacob_vejvoda.infernal_mobs.loot;

import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/** List of loot items and drop chance table */
public class LootConfig {

    public Map<String, LootItem> lootItems = new HashMap<>();
    public Map<Integer, Map<String, Double>> dropMap = new HashMap<>(); // Map<infernalLevel, Map<dropItemName, dropWeight>>

    LootConfig() {
    }

    public static <T> T weightedRandom(Map<T, Double> candidates) {
        if (candidates.size() <= 0) return null;
        double sum = 0;
        for (Double d : candidates.values()) sum += d;
        double random = LootManager.rnd.nextDouble() * sum;
        sum = 0;
        T ret = null;
        for (Map.Entry<T, Double> e : candidates.entrySet()) {
            sum += e.getValue();
            if (sum > random) {
                ret = e.getKey();
                break;
            }
        }
        return ret;
    }

    public LootItem getRandomDrop(int level) {
        if (!dropMap.containsKey(level)) {
            InfernalMobs.instance.getLogger().warning("No drop found for Level: " + level);
            return null;
        }
        String name = weightedRandom(dropMap.get(level));
        if (name == null) {
            InfernalMobs.instance.getLogger().warning("No drop found for Level: " + level);
            return null;
        }
        LootItem item = lootItems.get(name);
        if (item == null) {
            InfernalMobs.instance.getLogger().warning("Loot item not found:" + name);
            return null;
        }
        return item;
    }

    public static LootConfig parse(File f) {
        LootConfig l = new LootConfig();
        l.lootItems = new HashMap<>();
        l.dropMap = new HashMap<>();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection items = cfg.getConfigurationSection("lootItems");
        ConfigurationSection dropMap = cfg.getConfigurationSection("dropMap");
        for (String itemName : items.getKeys(false)) {
            ConfigurationSection s = items.getConfigurationSection(itemName);
            LootItem i = new LootItem();
            i.item = s.getItemStack("item");
            if (s.isString("amountRange")) i.amountRange = RangePair.parse(s.getString("amountRange"));
            if (s.isString("damageRange")) i.damageRange = RangePair.parse(s.getString("damageRange"));
            if (s.isList("commands")) i.commands = s.getStringList("commands");
            if (s.isConfigurationSection("extraEnchants")) {
                i.extraEnchants = new HashMap<>();
                ConfigurationSection sec = s.getConfigurationSection("extraEnchants");
                for (String ench : sec.getKeys(false)) {
                    i.extraEnchants.put(Enchantment.getByName(ench), RangePair.parse(sec.getString(ench)));
                }
            }
            if (s.isString("amountRange")) i.amountRange = RangePair.parse(s.getString("amountRange"));
            l.lootItems.put(itemName, i);
        }

        for (String levelKey : dropMap.getKeys(false)) {
            if (!levelKey.startsWith("level-")) continue;
            Integer level = Integer.parseInt(levelKey.substring(6));
            ConfigurationSection levelMap = dropMap.getConfigurationSection(levelKey);
            Map<String, Double> map = new HashMap<>();

            for (String dropName : levelMap.getKeys(false)) {
                map.put(dropName, levelMap.getDouble(dropName));
            }
            l.dropMap.put(level, map);
        }
        return l;
    }

    public void dump(File f) {
        YamlConfiguration cfg = new YamlConfiguration();
        ConfigurationSection items = cfg.createSection("lootItems");
        ConfigurationSection dropMap = cfg.createSection("dropMap");
        for (String name : lootItems.keySet()) {
            LootItem item = lootItems.get(name);
            ConfigurationSection s = items.createSection(name);
            s.set("item", item.item);
            if (item.amountRange != null) s.set("amountRange", item.amountRange.toString());
            if (item.damageRange != null) s.set("damageRange", item.damageRange.toString());
            if (item.commands != null) s.set("commands", item.commands);
            if (item.extraEnchants != null) {
                ConfigurationSection sec = s.createSection("extraEnchants");
                for (Enchantment e : item.extraEnchants.keySet()) {
                    sec.set(e.getName(), item.extraEnchants.get(e).toString());
                }
            }
        }

        for (Integer level : this.dropMap.keySet()) {
            dropMap.createSection("level-" + level.toString(), this.dropMap.get(level));
        }
        try {
            cfg.save(f);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.print(cfg.saveToString());
        }
    }

    public void setDropChance(Integer level, String name, Double chance) {
        if (chance <= 0) {
            if (dropMap.containsKey(level) && dropMap.get(level).containsKey(name)) {
                dropMap.get(level).remove(name);
                if (dropMap.get(level).size() <= 0) {
                    dropMap.remove(level);
                }
            }
        } else {
            if (!dropMap.containsKey(level)) dropMap.put(level, new HashMap<>());
            dropMap.get(level).put(name, chance);
        }
    }
}
