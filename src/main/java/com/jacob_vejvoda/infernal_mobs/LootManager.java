package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class LootManager {
    public static final Random rnd = new Random();

    public static class LootConfig {
        public static class RangePair {
            int min;
            int max;
            public RangePair(int min, int max) {
                this.min = min;
                this.max = max;
            }

            public int get() {
                return rnd.nextInt(max-min+1)+min;
            }
            @Override
            public String toString() {
                return String.format("%d-%d", min, max);
            }

            public static RangePair parse(String str) {
                try {
                    String[] arr = str.split("-");
                    int min = Integer.parseInt(arr[0]);
                    int max = Integer.parseInt(arr[1]);
                    if (min < max) return new RangePair(min, max);
                    else return new RangePair(max, min);
                } catch (Exception ex) {
                    infernal_mobs.instance.getLogger().warning("Bad range pair:"+str);
                    return null;
                }
            }
        }

        public static class LootItem {
            ItemStack item;
            RangePair damageRange;
            RangePair amountRange;
            Map<Enchantment, RangePair> extraEnchants;
            List<String> commands;
            /** get a randomized item to be given to player */
            public ItemStack get() {
                ItemStack ret = item.clone();
                if (damageRange != null) {
                    item.setDurability((short)damageRange.get());
                }
                if (amountRange != null) {
                    item.setAmount(amountRange.get());
                }
                if (extraEnchants != null) {
                    for (Enchantment e : extraEnchants.keySet()) {
                        item.addUnsafeEnchantment(e, extraEnchants.get(e).get());
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

        public Map<String, LootItem> lootItems;
        public Map<Integer, Map<EntityType, Map<String, Double>>> dropMap; // Map<infernalLevel, Map<entityType, Map<dropItemName, dropWeight>>>

        public static <T> T weightedRandom(Map<T, Double> candidates) {
            if (candidates.size() <= 0) return null;
            double sum = 0;
            for (Double d : candidates.values()) sum += d;
            double random = rnd.nextDouble() * sum;
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

        public LootItem getRandomDrop(EntityType entityType, int level) {
            if (!dropMap.containsKey(level)) {
                infernal_mobs.instance.getLogger().warning("No drop found for Level: " + level);
                return null;
            }
            if (!dropMap.get(level).containsKey(entityType)) {
                infernal_mobs.instance.getLogger().warning("No drop found for entityType: " + entityType.name() + " at level=" + Integer.toString(level));
                return null;
            }
            String name = weightedRandom(dropMap.get(level).get(entityType));
            if (name == null) {
                infernal_mobs.instance.getLogger().warning("No drop found for entityType: " + entityType.name() + " at level=" + Integer.toString(level));
                return null;
            }
            if (!lootItems.containsKey(name)) {
                infernal_mobs.instance.getLogger().warning("Loot item not found:" + entityType.name());
                return null;
            }
            return lootItems.get(name);
        }

        private LootConfig() {}
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
                Map<EntityType, Map<String, Double>> map = new HashMap<>();
                for (String entityName : levelMap.getKeys(false)) {
                    EntityType e = EntityType.valueOf(entityName);
                    ConfigurationSection entityMap = levelMap.getConfigurationSection(entityName);
                    Map<String, Double> map2 = new HashMap<>();
                    for (String dropName : entityMap.getKeys(false)) {
                        map2.put(dropName, entityMap.getDouble(dropName));
                    }
                    map.put(e, map2);
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
                        sec.set(e.getName(), item.extraEnchants.get(e).get());
                    }
                }
            }

            for (Integer level : this.dropMap.keySet()) {
                ConfigurationSection sec = dropMap.createSection("level-" + level.toString());
                for (Map.Entry<EntityType, Map<String, Double>> e : this.dropMap.get(level).entrySet()) {
                    sec.createSection(e.getKey().name(), e.getValue());
                }
            }
            try {
                cfg.save(f);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.print(cfg.saveToString());
            }
        }

        public void setDropChance(Integer level, EntityType e, String name, Double chance) {
            if (!dropMap.containsKey(level)) dropMap.put(level, new HashMap<>());
            if (!dropMap.get(level).containsKey(e)) dropMap.get(level).put(e, new HashMap<>());
            dropMap.get(level).get(e).put(name, chance);
        }
    }

    private final infernal_mobs plugin;
    public LootConfig cfg;
    public LootManager(infernal_mobs plugin) {
        this.plugin = plugin;
        if (new File(plugin.getDataFolder(),"loot_v2.yml").isFile()) { // loot file exists
            cfg = LootConfig.parse(new File(plugin.getDataFolder(),"loot_v2.yml"));
        } else if (new File(plugin.getDataFolder(),"loot.yml").isFile()) { // old config exists
            // TODO upgrade
        } else { // no config file
            cfg = new LootConfig();
            cfg.dump(new File(plugin.getDataFolder(),"loot_v2.yml"));
        }
    }

    public void reload() {
        if (new File(plugin.getDataFolder(),"loot_v2.yml").isFile()) { // loot file exists
            cfg = LootConfig.parse(new File(plugin.getDataFolder(),"loot_v2.yml"));
        } else if (new File(plugin.getDataFolder(),"loot.yml").isFile()) { // old config exists
            // TODO upgrade
        } else { // no config file
            cfg = new LootConfig();
            cfg.dump(new File(plugin.getDataFolder(),"loot_v2.yml"));
        }
    }

    public ItemStack getLootByName(Player p, String name) {
        if (!cfg.lootItems.containsKey(name)) return null;
        cfg.lootItems.get(name).applyCommands(p);
        return cfg.lootItems.get(name).get();
    }

    /**
     * Give random loot, NOTE: commands will be applied here.
     * @param player commands to be applied on
     * @param mob mob type name
     * @param powers mob power level
     * @return the loot, or null
     */
    public ItemStack getRandomLoot(final Player player, final String mob, final int powers) {
        LootConfig.LootItem loot = cfg.getRandomDrop(EntityType.valueOf(mob), powers);
        if (loot == null) return new ItemStack(Material.AIR);
        loot.applyCommands(player);
        return loot.get();
    }

    public void save() {
        cfg.dump(new File(plugin.getDataFolder(),"loot_v2.yml"));
    }

//    public ItemStack getItem(final int loot) {
//        try {
//            final int setItem = this.lootConfig.getInt("loot." + loot + ".item");
//            final String setAmountString = this.lootConfig.getString("loot." + loot + ".amount");
//            int setAmount;
//            if (setAmountString != null) {
//                setAmount = this.getIntFromString(setAmountString);
//            } else {
//                setAmount = 1;
//            }
//            final ItemStack stack = new ItemStack(setItem, setAmount);
//            if (this.lootConfig.getString("loot." + loot + ".durability") != null) {
//                final String durabilityString = this.lootConfig.getString("loot." + loot + ".durability");
//                final int durability = this.getIntFromString(durabilityString);
//                stack.setDurability((short) durability);
//            }
//            String name = null;
//            if (this.lootConfig.getString("loot." + loot + ".name") != null && this.lootConfig.isString("loot." + loot + ".name")) {
//                name = this.lootConfig.getString("loot." + loot + ".name");
//                name = this.prosessLootName(name, stack);
//            } else if (this.lootConfig.isList("loot." + loot + ".name")) {
//                final ArrayList<String> names = (ArrayList<String>) this.lootConfig.getList("loot." + loot + ".name");
//                if (names != null) {
//                    name = names.get(Helper.rand(1, names.size()) - 1);
//                    name = this.prosessLootName(name, stack);
//                }
//            }
//            final ArrayList<String> loreList = new ArrayList<String>();
//            for (int i = 0; i <= 32; ++i) {
//                if (this.lootConfig.getString("loot." + loot + ".lore" + i) != null) {
//                    String lore = this.lootConfig.getString("loot." + loot + ".lore" + i);
//                    lore = ChatColor.translateAlternateColorCodes('&', lore);
//                    loreList.add(lore);
//                }
//            }
//            if (this.lootConfig.getList("loot." + loot + ".lore") != null) {
//                final ArrayList<String> lb = (ArrayList<String>) this.lootConfig.getList("loot." + loot + ".lore");
//                final ArrayList<String> l = (ArrayList<String>) lb.clone();
//                int min = l.size();
//                if (this.lootConfig.getString("loot." + loot + ".minLore") != null) {
//                    min = this.lootConfig.getInt("loot." + loot + ".minLore");
//                }
//                int max = l.size();
//                if (this.lootConfig.getString("loot." + loot + ".maxLore") != null) {
//                    max = this.lootConfig.getInt("loot." + loot + ".maxLore");
//                }
//                if (!l.isEmpty()) {
//                    for (int j = 0; j < Helper.rand(min, max); ++j) {
//                        final String lore2 = l.get(Helper.rand(1, l.size()) - 1);
//                        l.remove(lore2);
//                        loreList.add(this.prosessLootName(lore2, stack));
//                    }
//                }
//            }
//            final ItemMeta meta = stack.getItemMeta();
//            if (name != null) {
//                meta.setDisplayName(name);
//            }
//            if (!loreList.isEmpty()) {
//                meta.setLore((List) loreList);
//            }
//            if (meta != null) {
//                stack.setItemMeta(meta);
//            }
//            if (this.lootConfig.getString("loot." + loot + ".colour") != null && stack.getType().toString().toLowerCase().contains("leather")) {
//                final String c = this.lootConfig.getString("loot." + loot + ".colour");
//                final String[] split = c.split(",");
//                final Color colour = Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
//                Helper.changeLeatherColor(stack, colour);
//            }
//            if (stack.getType().equals((Object) Material.WRITTEN_BOOK) || stack.getType().equals((Object) Material.BOOK_AND_QUILL)) {
//                final BookMeta bMeta = (BookMeta) stack.getItemMeta();
//                if (this.lootConfig.getString("loot." + loot + ".author") != null) {
//                    String author = this.lootConfig.getString("loot." + loot + ".author");
//                    author = ChatColor.translateAlternateColorCodes('&', author);
//                    bMeta.setAuthor(author);
//                }
//                if (this.lootConfig.getString("loot." + loot + ".title") != null) {
//                    String title = this.lootConfig.getString("loot." + loot + ".title");
//                    title = ChatColor.translateAlternateColorCodes('&', title);
//                    bMeta.setTitle(title);
//                }
//                if (this.lootConfig.getString("loot." + loot + ".pages") != null) {
//                    for (final String k : this.lootConfig.getConfigurationSection("loot." + loot + ".pages").getKeys(false)) {
//                        String page = this.lootConfig.getString("loot." + loot + ".pages." + k);
//                        page = ChatColor.translateAlternateColorCodes('&', page);
//                        bMeta.addPage(new String[]{page});
//                    }
//                }
//                stack.setItemMeta((ItemMeta) bMeta);
//            }
//            if (stack.getType().equals((Object) Material.BANNER)) {
//                final BannerMeta b = (BannerMeta) stack.getItemMeta();
//                final List<Pattern> patList = (List<Pattern>) this.lootConfig.getList("loot." + loot + ".patterns");
//                if (patList != null && !patList.isEmpty()) {
//                    b.setPatterns((List) patList);
//                }
//                stack.setItemMeta((ItemMeta) b);
//            }
//            if (stack.getType().equals((Object) Material.SKULL_ITEM) && stack.getDurability() == 3) {
//                final String owner = this.lootConfig.getString("loot." + loot + ".owner");
//                final SkullMeta sm = (SkullMeta) stack.getItemMeta();
//                sm.setOwner(owner);
//                stack.setItemMeta((ItemMeta) sm);
//            }
//            int enchAmount = 0;
//            for (int e = 0; e <= 10; ++e) {
//                if (this.lootConfig.getString("loot." + loot + ".enchantments." + e) != null) {
//                    ++enchAmount;
//                }
//            }
//            if (enchAmount > 0) {
//                int enMin = enchAmount;
//                int enMax = enchAmount;
//                if (this.lootConfig.getString("loot." + loot + ".minEnchantments") != null && this.lootConfig.getString("loot." + loot + ".maxEnchantments") != null) {
//                    enMin = this.lootConfig.getInt("loot." + loot + ".minEnchantments");
//                    enMax = this.lootConfig.getInt("loot." + loot + ".maxEnchantments");
//                }
//                int enchNeeded = new Random().nextInt(enMax + 1 - enMin) + enMin;
//                if (enchNeeded > enMax) {
//                    enchNeeded = enMax;
//                }
//                final ArrayList<LevelledEnchantment> enchList = new ArrayList<LevelledEnchantment>();
//                int safety = 0;
//                int m = 0;
//                do {
//                    if (this.lootConfig.getString("loot." + loot + ".enchantments." + m) != null) {
//                        int enChance = 1;
//                        if (this.lootConfig.getString("loot." + loot + ".enchantments." + m + ".chance") != null) {
//                            enChance = this.lootConfig.getInt("loot." + loot + ".enchantments." + m + ".chance");
//                        }
//                        final int chance = new Random().nextInt(enChance - 1 + 1) + 1;
//                        if (chance == 1) {
//                            final String enchantment = this.lootConfig.getString("loot." + loot + ".enchantments." + m + ".enchantment");
//                            final String levelString = this.lootConfig.getString("loot." + loot + ".enchantments." + m + ".level");
//                            int level = this.getIntFromString(levelString);
//                            if (Enchantment.getByName(enchantment) == null) {
//                                System.out.println("Error: No valid drops found!");
//                                System.out.println("Error: " + enchantment + " is not a valid enchantment!");
//                                return null;
//                            }
//                            if (level < 1) {
//                                level = 1;
//                            }
//                            final LevelledEnchantment le = new LevelledEnchantment(Enchantment.getByName(enchantment), level);
//                            boolean con = false;
//                            for (final LevelledEnchantment testE : enchList) {
//                                if (testE.getEnchantment.equals((Object) le.getEnchantment)) {
//                                    con = true;
//                                }
//                            }
//                            if (!con) {
//                                enchList.add(le);
//                            }
//                        }
//                    }
//                    if (++m > enchAmount) {
//                        m = 0;
//                        ++safety;
//                    }
//                    if (safety >= enchAmount * 100) {
//                        System.out.println("Error: No valid drops found!");
//                        System.out.println("Error: Please increase chance for enchantments on item " + loot);
//                        return null;
//                    }
//                } while (enchList.size() != enchNeeded);
//                for (final LevelledEnchantment le2 : enchList) {
//                    if (stack.getType().equals((Object) Material.ENCHANTED_BOOK)) {
//                        final EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) stack.getItemMeta();
//                        enchantMeta.addStoredEnchant(le2.getEnchantment, le2.getLevel, true);
//                        stack.setItemMeta((ItemMeta) enchantMeta);
//                    } else {
//                        stack.addUnsafeEnchantment(le2.getEnchantment, le2.getLevel);
//                    }
//                }
//            }
//            return stack;
//        } catch (Exception e2) {
//            this.getLogger().log(Level.SEVERE, e2.getMessage(), true);
//            e2.printStackTrace();
//            return null;
//        }
//    }
//
//    private String prosessLootName(String name, final ItemStack stack) {
//        name = ChatColor.translateAlternateColorCodes('&', name);
//        String itemName = stack.getType().name();
//        itemName = itemName.replace("_", " ");
//        itemName = itemName.toLowerCase();
//        name = name.replace("<itemName>", itemName);
//        return name;
//    }
}
