package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

            public static RangePair parse(String str) {
                try {
                    String[] arr = str.split("-");
                    int min = Integer.parseInt(arr[0]);
                    int max = Integer.parseInt(arr[1]);
                    if (min < max) return new RangePair(min, max);
                    else return new RangePair(max, min);
                } catch (Exception ex) {
                    infernal_mobs.instance.getLogger().warning("Bad range pair:" + str);
                    return null;
                }
            }

            public int get() {
                return rnd.nextInt(max - min + 1) + min;
            }

            @Override
            public String toString() {
                return String.format("%d-%d", min, max);
            }
        }

        public static class LootItem {
            ItemStack item;
            RangePair damageRange;
            RangePair amountRange;
            Map<Enchantment, RangePair> extraEnchants;
            List<String> commands;

            /**
             * get a randomized item to be given to player
             */
            public ItemStack get() {
                ItemStack ret = item.clone();
                if (damageRange != null) {
                    short damageR = (short) damageRange.get();
                    if (damageR < 0) damageR = 0;
                    if (damageR >= item.getType().getMaxDurability())
                        damageR = (short) (item.getType().getMaxDurability() - 1);
                    item.setDurability(damageR);
                }
                if (amountRange != null) {
                    item.setAmount(amountRange.get());
                }
                if (extraEnchants != null) {
                    if (item.getType() == Material.ENCHANTED_BOOK) {
                        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                        for (Enchantment e : extraEnchants.keySet()) {
                            meta.addStoredEnchant(e, extraEnchants.get(e).get(), true);
                        }
                        item.setItemMeta(meta);
                    } else {
                        for (Enchantment e : extraEnchants.keySet()) {
                            item.addUnsafeEnchantment(e, extraEnchants.get(e).get());
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

        public Map<String, LootItem> lootItems;
        public Map<Integer, Map<EntityType, Map<String, Double>>> dropMap; // Map<infernalLevel, Map<entityType, Map<dropItemName, dropWeight>>>

        private LootConfig() {
        }

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
                        sec.set(e.getName(), item.extraEnchants.get(e).toString());
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
        if (new File(plugin.getDataFolder(), "loot_v2.yml").isFile()) { // loot file exists
            cfg = LootConfig.parse(new File(plugin.getDataFolder(), "loot_v2.yml"));
        } else if (new File(plugin.getDataFolder(), "loot.yml").isFile()) { // old config exists
            cfg = parseOldConfig(new File(plugin.getDataFolder(), "loot.yml"));
            cfg.dump(new File(plugin.getDataFolder(), "loot_v2.yml"));
        } else { // no config file
            cfg = new LootConfig();
            cfg.dump(new File(plugin.getDataFolder(), "loot_v2.yml"));
        }
    }


    private static LootConfig parseOldConfig(File f) {
        LootConfig cfg = new LootConfig();
        cfg.dropMap = new HashMap<>();
        cfg.lootItems = new HashMap<>();
        YamlConfiguration sec = YamlConfiguration.loadConfiguration(f);
        List<EntityType> livingEntityTypes = Stream.of(EntityType.values()).filter(EntityType::isAlive).filter(e->e!=EntityType.PLAYER).collect(Collectors.toList());
        for (String itemIdx : sec.getConfigurationSection("loot").getKeys(false)) {
            ConfigurationSection s = sec.getConfigurationSection("loot." + itemIdx);
            LootConfig.LootItem l = getLootFromOldConfig(Integer.parseInt(itemIdx), sec);
            Integer minLevel = s.getInt("powerMin", 0);
            Integer maxLevel = s.getInt("powerMax", 24);
            Double chance = (double)s.getInt("chancePercentage", 100);
            if (maxLevel < minLevel) {
                Integer tmp = minLevel;
                minLevel = maxLevel;
                maxLevel = tmp;
            }
            List<EntityType> e = s.isList("mobs")?
                    (s.getStringList("mobs").stream().filter(t -> t instanceof String).map(t -> EntityType.valueOf(t))
                    .filter(t->t!=null).collect(Collectors.toList()))
                    :
                    livingEntityTypes;
            for (Integer lv = minLevel; lv <= maxLevel; lv++) {
                for (EntityType t : e) {
                    cfg.setDropChance(lv, t, itemIdx, chance);
                }
            }
            cfg.lootItems.put(itemIdx, l);
        }
        return cfg;
    }

    private static LootConfig.LootItem getLootFromOldConfig(int oldIndex, ConfigurationSection oldCfgRoot) {
        LootConfig.LootItem i = new LootConfig.LootItem();
        Map<Enchantment, LootConfig.RangePair> optionalEnchList = new HashMap<>();
        i.item = getItem(oldIndex, oldCfgRoot, optionalEnchList);
        ConfigurationSection sec = oldCfgRoot.getConfigurationSection("loot." + oldIndex);
        if (sec.isString("amount") && sec.getString("amount").contains("-"))
            i.amountRange = LootConfig.RangePair.parse(sec.getString("amount"));
        if (sec.isString("durability") && sec.getString("durability").contains("-"))
            i.damageRange = LootConfig.RangePair.parse(sec.getString("durability"));
        if (optionalEnchList.size() > 0)
            i.extraEnchants = optionalEnchList;
        if (sec.isList("commands"))
            i.commands = sec.getStringList("commands").stream().filter(s -> s instanceof String)
                    .map(s -> s.replace("<player>", "{player}")).collect(Collectors.toList());
        return i;
    }

    private static ItemStack getItem(final int lootIdx, ConfigurationSection lootConfig, Map<Enchantment, LootConfig.RangePair> optionalEnchList) {
        try {
            final int setItem = lootConfig.getInt("loot." + lootIdx + ".item");
            final String setAmountString = lootConfig.getString("loot." + lootIdx + ".amount");
            int setAmount;
            if (setAmountString != null) {
                setAmount = getIntFromString(setAmountString);
            } else {
                setAmount = 1;
            }
            final ItemStack stack = new ItemStack(setItem, setAmount);
            if (lootConfig.getString("loot." + lootIdx + ".durability") != null) {
                final String durabilityString = lootConfig.getString("loot." + lootIdx + ".durability");
                final int durability = getIntFromString(durabilityString);
                stack.setDurability((short) durability);
            }
            String name = null;
            if (lootConfig.getString("loot." + lootIdx + ".name") != null && lootConfig.isString("loot." + lootIdx + ".name")) {
                name = lootConfig.getString("loot." + lootIdx + ".name");
                name = processLoreName(name, stack);
            } else if (lootConfig.isList("loot." + lootIdx + ".name")) {
                final ArrayList<String> names = (ArrayList<String>) lootConfig.getList("loot." + lootIdx + ".name");
                if (names != null) {
                    //name = names.get(Helper.rand(1, names.size()) - 1);
                    name = names.get(0);
                    name = processLoreName(name, stack);
                }
            }
            final ArrayList<String> loreList = new ArrayList<String>();
            for (int i = 0; i <= 32; ++i) {
                if (lootConfig.getString("loot." + lootIdx + ".lore" + i) != null) {
                    String lore = lootConfig.getString("loot." + lootIdx + ".lore" + i);
                    lore = ChatColor.translateAlternateColorCodes('&', lore);
                    loreList.add(lore);
                }
            }
            if (lootConfig.getList("loot." + lootIdx + ".lore") != null) {
                loreList.addAll(lootConfig.getStringList("loot." + lootIdx + ".lore").stream()
                        .map(s -> processLoreName(s, stack)).collect(Collectors.toList()));
            }
            final ItemMeta meta = stack.getItemMeta();
            if (name != null) {
                meta.setDisplayName(name);
            }
            if (!loreList.isEmpty()) {
                meta.setLore(loreList);
            }
            if (meta != null) {
                stack.setItemMeta(meta);
            }
            if (lootConfig.getString("loot." + lootIdx + ".colour") != null && stack.getType().toString().toLowerCase().contains("leather")) {
                final String c = lootConfig.getString("loot." + lootIdx + ".colour");
                final String[] split = c.split(",");
                final Color colour = Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                Helper.changeLeatherColor(stack, colour);
            }
            if (stack.getType().equals(Material.WRITTEN_BOOK) || stack.getType().equals(Material.BOOK_AND_QUILL)) {
                final BookMeta bMeta = (BookMeta) stack.getItemMeta();
                if (lootConfig.getString("loot." + lootIdx + ".author") != null) {
                    String author = lootConfig.getString("loot." + lootIdx + ".author");
                    author = ChatColor.translateAlternateColorCodes('&', author);
                    bMeta.setAuthor(author);
                }
                if (lootConfig.getString("loot." + lootIdx + ".title") != null) {
                    String title = lootConfig.getString("loot." + lootIdx + ".title");
                    title = ChatColor.translateAlternateColorCodes('&', title);
                    bMeta.setTitle(title);
                }
                if (lootConfig.getString("loot." + lootIdx + ".pages") != null) {
                    for (final String k : lootConfig.getConfigurationSection("loot." + lootIdx + ".pages").getKeys(false)) {
                        String page = lootConfig.getString("loot." + lootIdx + ".pages." + k);
                        page = ChatColor.translateAlternateColorCodes('&', page);
                        bMeta.addPage(page);
                    }
                }
                stack.setItemMeta(bMeta);
            }
            if (stack.getType().equals(Material.BANNER)) {
                final BannerMeta b = (BannerMeta) stack.getItemMeta();
                final List<Pattern> patList = (List<Pattern>) lootConfig.getList("loot." + lootIdx + ".patterns");
                if (patList != null && !patList.isEmpty()) {
                    b.setPatterns(patList);
                }
                stack.setItemMeta(b);
            }
            if (stack.getType().equals(Material.SKULL_ITEM) && stack.getDurability() == 3) {
                final String owner = lootConfig.getString("loot." + lootIdx + ".owner");
                final SkullMeta sm = (SkullMeta) stack.getItemMeta();
                sm.setOwner(owner);
                stack.setItemMeta(sm);
            }

            if (lootConfig.isConfigurationSection("loot." + lootIdx + ".enchantments")) {
                for (String key : lootConfig.getConfigurationSection("loot." + lootIdx + ".enchantments").getKeys(false)) {
                    ConfigurationSection sec = lootConfig.getConfigurationSection("loot." + lootIdx + ".enchantments." + key);
                    String n = sec.getString("enchantment");
                    String l = sec.getString("level");
                    Enchantment e = Enchantment.getByName(n);
                    if (e == null) {
                        infernal_mobs.instance.getLogger().warning("invalid ench name: " + n);
                        continue;
                    }
                    if (l.contains("-")) {
                        LootConfig.RangePair p = LootConfig.RangePair.parse(l);
                        optionalEnchList.put(e, p);
                    } else {
                        Integer level = Integer.parseInt(l);
                        if (stack.getType().equals(Material.ENCHANTED_BOOK)) {
                            final EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) stack.getItemMeta();
                            enchantMeta.addStoredEnchant(e, level, true);
                            stack.setItemMeta(enchantMeta);
                        } else {
                            stack.addUnsafeEnchantment(e, level);
                        }
                    }
                }
            }
            return stack;
        } catch (Exception e2) {
            infernal_mobs.instance.getLogger().log(Level.SEVERE, e2.getMessage(), true);
            e2.printStackTrace();
            return null;
        }
    }

    static private String processLoreName(String name, final ItemStack stack) {
        name = ChatColor.translateAlternateColorCodes('&', name);
        return name;
    }

    static private int getIntFromString(final String setAmountString) {
        int setAmount = 1;
        if (setAmountString.contains("-")) {
            final String[] split = setAmountString.split("-");
            try {
                final Integer minSetAmount = Integer.parseInt(split[0]);
                final Integer maxSetAmount = Integer.parseInt(split[1]);
                setAmount = new Random().nextInt(maxSetAmount - minSetAmount + 1) + minSetAmount;
            } catch (Exception e) {
                System.out.println("getIntFromString: " + e);
            }
        } else {
            setAmount = Integer.parseInt(setAmountString);
        }
        return setAmount;
    }

    public void reload() {
        if (new File(plugin.getDataFolder(), "loot_v2.yml").isFile()) { // loot file exists
            cfg = LootConfig.parse(new File(plugin.getDataFolder(), "loot_v2.yml"));
        } else if (new File(plugin.getDataFolder(), "loot.yml").isFile()) { // old config exists
            cfg = parseOldConfig(new File(plugin.getDataFolder(), "loot.yml"));
            cfg.dump(new File(plugin.getDataFolder(), "loot_v2.yml"));
        } else { // no config file
            cfg = new LootConfig();
            cfg.dump(new File(plugin.getDataFolder(), "loot_v2.yml"));
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
     * @param mob    mob type name
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
        cfg.dump(new File(plugin.getDataFolder(), "loot_v2.yml"));
    }

}
