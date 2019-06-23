package com.jacob_vejvoda.infernal_mobs;

import cat.nyaa.nyaacore.utils.InventoryUtils;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.api.InfernalMobSpawnEvent;
import com.jacob_vejvoda.infernal_mobs.api.InfernalSpawnReason;
import com.jacob_vejvoda.infernal_mobs.config.CustomMobConfig;
import com.jacob_vejvoda.infernal_mobs.loot.legacy.LootItem;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static cat.nyaa.nyaacore.CommandReceiver.Arguments;

public class CommandHandler implements CommandExecutor {
    private static final class NotPlayerException extends RuntimeException { }
    private static Player asPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return (Player)sender;
        } else {
            throw new NotPlayerException();
        }
    }

    private final InfernalMobs plugin;

    public CommandHandler(InfernalMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!sender.hasPermission("infernal_mobs.commands")) {
            sender.sendMessage("You don't have permission to use this command!");
            return true;
        }

        Arguments arg = Arguments.parse(args);
        if (arg == null) return false;
        String subcommand = arg.next();
        if (subcommand == null) subcommand = "help";

        try {
            if ("help".equalsIgnoreCase(subcommand)) {
                printHelp(sender);
            } else if ("reload".equalsIgnoreCase(subcommand)) {
                plugin.reloadConfig();
                ConfigReader.reload();
                plugin.reloadLoot();
                InfernalMobs.instance.reloadMainLoopTask();
                EnumAbilities.reloadAbility();
                sender.sendMessage("§eConfig reloaded!");
            } else if ("mobList".equalsIgnoreCase(subcommand)) {
                sender.sendMessage("Mob List:");
                for (EntityType t : EntityType.values()) {
                    sender.sendMessage(t.name());
                }
            } else if ("error".equalsIgnoreCase(subcommand)) {
                plugin.errorList.add(asPlayer(sender));
                sender.sendMessage("Click on a mob to send an error report about it.");
            } else if ("info".equalsIgnoreCase(subcommand)) {
                sender.sendMessage("Mounts: " + plugin.mobManager.mounteeMobs.size());
                sender.sendMessage("Infernals: " + plugin.mobManager.mobMap.size());
            } else if ("worldInfo".equalsIgnoreCase(subcommand)) {
                final World world = asPlayer(sender).getWorld();
                String enabled = ConfigReader.isEnabledWorld(world) ? "is" : "is not";
                sender.sendMessage("The world you are currently in, " + world + " " + enabled + " enabled.");
                sender.sendMessage("All the world that are enabled are: ");
                for (World w : Bukkit.getWorlds()) {
                    if (ConfigReader.isEnabledWorld(w)) {
                        sender.sendMessage("- " + w.getName());
                    }
                }
            } else if ("abilities".equalsIgnoreCase(subcommand)) {
                printAbilities(sender);
            } else if ("addloot".equalsIgnoreCase(subcommand)) {
                if (arg.top() == null) {
                    sender.sendMessage("usage: /im addloot <name>");
                    return true;
                }
                ItemStack item = asPlayer(sender).getInventory().getItemInMainHand();
                if (item == null || item.getType() == Material.AIR) {
                    sender.sendMessage("Add fail. Please check if you are holding the item");
                    return true;
                }
                String name = arg.nextString();
                LootItem li = new LootItem();
                li.item = item.clone();
                if (plugin.lootManager.cfg.lootItems.containsKey(name)) {
                    sender.sendMessage("Fail. Duplicated name");
                    return true;
                }
                plugin.lootManager.cfg.lootItems.put(name, li);
                plugin.lootManager.save();
                sender.sendMessage("Item Added.");
            } else if ("checkchance".equalsIgnoreCase(subcommand)) {
                if ("level".equalsIgnoreCase(arg.nextString())) {
                    int level = arg.nextInt();
                    Map<String, Double> m = plugin.lootManager.cfg.dropMap.get(level);
                    Double sum = m.values().stream().mapToDouble(Double::doubleValue).sum();
                    sender.sendMessage(String.format("Listing drop chance for level %d", level));
                    m.entrySet().stream().sorted((a,b)->a.getValue().compareTo(b.getValue()))
                            .forEach(e -> sender.sendMessage(String.format("  %3.03f%% %s", e.getValue() / sum * 100D, getLootDisplayName(e.getKey()))));
                } else {
                    String itemName = arg.nextString();
                    Map2D<Integer, String, Double> map = new Map2D<>();
                    for (Map.Entry<Integer, Map<String, Double>> e : plugin.lootManager.cfg.dropMap.entrySet()) {
                        map.setRow(e.getKey(), normalize(e.getValue()));
                    }
                    sender.sendMessage(String.format("Listing drop chance for item \"%s\"", itemName));
                    Map<Integer, Double> m = map.getColumn(itemName);
                    if (m.size() == 0) {
                        sender.sendMessage("Item never dropped");
                    } else {
                        normalize(m).entrySet().stream()
                                .sorted((a,b)->a.getKey().compareTo(b.getKey()))
                                .forEach(e->sender.sendMessage(String.format("  SpawnConfig%2d: %.03f%%", e.getKey(), e.getValue()*100D)));
                    }
                }
            } else if ("setdrop".equalsIgnoreCase(subcommand)) {
                int level = arg.nextInt();
                String name = arg.nextString();
                double w = arg.nextDouble();
                plugin.lootManager.cfg.setDropChance(level, name, w);
                plugin.lootManager.save();
                sender.sendMessage("Chance set.");
            } else if ("killall".equalsIgnoreCase(subcommand)) {
                String worldName = arg.nextString();
                World w = plugin.getServer().getWorld(worldName);
                if (w == null) {
                    sender.sendMessage("World not found!");
                    return true;
                }
                for (Entity e : w.getEntities()) {
                    if (plugin.mobManager.mobMap.containsKey(e.getUniqueId())) {
                        plugin.mobManager.mobMap.remove(e.getUniqueId());
                        e.remove();
                    }
                }
                sender.sendMessage("Killed all loaded infernal mobs in that world!");
            } else if ("kill".equalsIgnoreCase(subcommand)) {
                int radius = arg.nextInt();
                for (Entity e : asPlayer(sender).getNearbyEntities(radius, radius, radius)) {
                    if (plugin.mobManager.mobMap.containsKey(e.getUniqueId())) {
                        plugin.mobManager.mobMap.remove(e.getUniqueId());
                        e.remove();
                    }
                }
                sender.sendMessage("Killed all infernal mobs near you!");
            } else if ("getloot".equalsIgnoreCase(subcommand)) {
                if (arg.top() == null) {
                    Player player = asPlayer(sender);
                    final int powers = Helper.rand(ConfigReader.getMinimalLevel(), ConfigReader.getMaximumLevel());
                    final ItemStack gottenLoot = plugin.lootManager.getRandomLoot(player, powers);
                    if (gottenLoot != null && gottenLoot.getType() != Material.AIR) {
                        if (!InventoryUtils.addItem(player, gottenLoot)) {
                            Location location = player.getLocation();
                            player.getWorld().dropItem(location, gottenLoot);
                        }
                        if (sender.isOp()) {
                            sender.sendMessage("§eGave you some random loot!");
                        }
                    }
                } else {
                    String name = arg.nextString();
                    ItemStack i = plugin.lootManager.getLootByName(asPlayer(sender), name);
                    if (i != null && i.getType() != Material.AIR) {
                        Player player = asPlayer(sender);
                        if (!InventoryUtils.addItem(player, i)) {
                            Location location = player.getLocation();
                            player.getWorld().dropItem(location, i);
                        }
                        if (sender.isOp()) {
                            sender.sendMessage("§eGave you the loot: " + name);
                        }
                    }
                }
            } else if ("spawn".equalsIgnoreCase(subcommand)) {
                EntityType type = arg.nextEnum(EntityType.class);
                Location farSpawnLoc = asPlayer(sender).getTargetBlock((Set<Material>) null, 200).getLocation();
                farSpawnLoc.setY(farSpawnLoc.getY() + 1.0);
                List<EnumAbilities> abilities = new ArrayList<>();
                if (arg.top() == null) { // random ability
                    abilities = Helper.randomNItems(ConfigReader.getEnabledAbilities(), MobManager.getInfernalLevelForLocation(farSpawnLoc));
                } else { // ability list
                    while (arg.top() != null) abilities.add(arg.nextEnum(EnumAbilities.class));
                }
                if (abilities.size() <= 0) {
                    sender.sendMessage("No ability selected");
                } else {
                    if (plugin.mobManager.spawnMob(type, farSpawnLoc, abilities, InfernalSpawnReason.COMMAND) != null) {
                        sender.sendMessage("Mob spawned");
                    } else {
                        sender.sendMessage("Cannot spawn mob");
                    }
                }
            } else if ("sm".equalsIgnoreCase(subcommand)){
                String mobName = arg.nextString();
                if ("list".equalsIgnoreCase(mobName)){
                    Map<String, CustomMobConfig.CustomMob> customMobs = ConfigReader.getCustomMobConfig().getCustomMobs();
                    Set<String> names = customMobs.keySet();
                    if (names.isEmpty()){
                        if (sender.isOp()){
                            sender.sendMessage("no custom mobs found");
                        }
                    }else {
                        if (sender.isOp()){
                            String message = "&aHere "+ (names.size() == 1? "is ": "are ")+names.size()+" mob"+(names.size()==1?"":"s");
                            message = ChatColor.translateAlternateColorCodes('&', message);
                            sender.sendMessage(message);
                            names.forEach(s -> {
                                sender.sendMessage(s);
                            });
                        }
                    }
                    return true;
                }
                Location farSpawnLoc = asPlayer(sender).getTargetBlock((Set<Material>) null, 200).getLocation();
                CustomMobConfig mbConf = ConfigReader.getCustomMobConfig();
                CustomMobConfig.CustomMob cm = mbConf.getByName(mobName);
                List<EnumAbilities> abilities = new ArrayList<>();
                mbConf.setAbilities(abilities, cm);
                Mob mob = mbConf.spawnCustomMob(plugin.mobManager, farSpawnLoc, abilities, cm);
                String top = arg.top();
                if (top !=null) {
                    cm.spawnLevel = Integer.parseInt(top);
                }else {
                    cm.spawnLevel = cm.smSpawnLevel == -1 ?
                            ConfigReader.getLevelConfig().getLevel(farSpawnLoc.distance(farSpawnLoc.getWorld().getSpawnLocation()))
                            : cm.smSpawnLevel;
                }
                mbConf.addCustomAttr(mob, cm);
                LivingEntity entity = (LivingEntity) InfernalMobs.instance.getServer().getEntity(mob.entityId);
                InfernalMobSpawnEvent event = new InfernalMobSpawnEvent(entity, mob, null, InfernalSpawnReason.COMMAND);
                MobManager.setInfernalMobName(event);
                String spawned = "&aspawned &e"+cm.name+" &alevel &e"+cm.spawnLevel;
                spawned = ChatColor.translateAlternateColorCodes('&',spawned);
                sender.sendMessage(spawned);
            } else if ("csm".equalsIgnoreCase(subcommand)){
                String mobName = arg.nextString();
                if ("list".equalsIgnoreCase(mobName)){
                    Map<String, CustomMobConfig.CustomMob> customMobs = ConfigReader.getCustomMobConfig().getCustomMobs();
                    Set<String> names = customMobs.keySet();
                    if (names.isEmpty()){
                        if (sender.isOp()){
                            sender.sendMessage("no custom mobs found");
                        }
                    }else {
                        if (sender.isOp()) {
                            String message = "&aHere " + (names.size() == 1 ? "is " : "are ") + names.size() + " mob" + (names.size() == 1 ? "" : "s");
                            message = ChatColor.translateAlternateColorCodes('&', message);
                            sender.sendMessage(message);
                            names.forEach(s -> {
                                sender.sendMessage(s);
                            });
                        }
                    }
                    return true;
                }
                String worldName = arg.nextString();
                World w = plugin.getServer().getWorld(worldName);
                if (w == null) {
                    sender.sendMessage("World not found!");
                    return true;
                }
                double x = arg.nextDouble();
                double y = arg.nextDouble();
                double z = arg.nextDouble();
                Location loc = new Location(w, x,y,z);
                CustomMobConfig mbConf = ConfigReader.getCustomMobConfig();
                CustomMobConfig.CustomMob cm = mbConf.getByName(mobName);
                List<EnumAbilities> abilities = new ArrayList<>();
                mbConf.setAbilities(abilities, cm);
                Mob mob = mbConf.spawnCustomMob(plugin.mobManager, loc, abilities, cm);
                String top = arg.top();
                if (top !=null) {
                    cm.spawnLevel = Integer.parseInt(top);
                }else {
                    cm.spawnLevel = cm.smSpawnLevel == -1 ?
                            ConfigReader.getLevelConfig().getLevel(loc.distance(loc.getWorld().getSpawnLocation()))
                            : cm.smSpawnLevel;
                }
                mbConf.addCustomAttr(mob, cm);
                LivingEntity entity = (LivingEntity) InfernalMobs.instance.getServer().getEntity(mob.entityId);
                InfernalMobSpawnEvent event = new InfernalMobSpawnEvent(entity, mob, null, InfernalSpawnReason.COMMAND);
                MobManager.setInfernalMobName(event);
                String spawned = "&aspawned &e"+cm.name+" &alevel &e"+cm.spawnLevel;
                spawned = ChatColor.translateAlternateColorCodes('&',spawned);
                sender.sendMessage(spawned);
            }else if ("cspawn".equalsIgnoreCase(subcommand)) {
                EntityType type = arg.nextEnum(EntityType.class);
                String worldName = arg.nextString();
                World w = plugin.getServer().getWorld(worldName);
                if (w == null) {
                    sender.sendMessage("World not found!");
                    return true;
                }
                double x = arg.nextDouble();
                double y = arg.nextDouble();
                double z = arg.nextDouble();
                Location loc = new Location(w, x,y,z);
                List<EnumAbilities> abilities = new ArrayList<>();
                if (arg.top() == null) { // random ability
                    abilities = Helper.randomNItems(ConfigReader.getEnabledAbilities(), MobManager.getInfernalLevelForLocation(loc));
                } else { // ability list
                    while (arg.top() != null) abilities.add(arg.nextEnum(EnumAbilities.class));
                }
                if (abilities.size() <= 0) {
                    sender.sendMessage("No ability selected");
                } else {
                    if (plugin.mobManager.spawnMob(type, loc, abilities, InfernalSpawnReason.COMMAND) != null) {
                        sender.sendMessage("Mob spawned");
                    } else {
                        sender.sendMessage("Cannot spawn mob");
                    }
                }
            } else if ("pspawn".equalsIgnoreCase(subcommand)) {
                EntityType type = arg.nextEnum(EntityType.class);
                String playerName = arg.nextString();
                Player p = Bukkit.getPlayer(playerName);
                if (p == null) {
                    sender.sendMessage("Player not found: " + playerName);
                    return true;
                }
                Location loc = p.getLocation();
                List<EnumAbilities> abilities = new ArrayList<>();
                if (arg.top() == null) { // random ability
                    abilities = Helper.randomNItems(ConfigReader.getEnabledAbilities(), MobManager.getInfernalLevelForLocation(loc));
                } else { // ability list
                    while (arg.top() != null) abilities.add(arg.nextEnum(EnumAbilities.class));
                }
                if (abilities.size() <= 0) {
                    sender.sendMessage("No ability selected");
                } else {
                    if (plugin.mobManager.spawnMob(type, loc, abilities, InfernalSpawnReason.COMMAND) != null) {
                        sender.sendMessage("Mob spawned");
                    } else {
                        sender.sendMessage("Cannot spawn mob");
                    }
                }
            }
        } catch (NotPlayerException ex) {
            sender.sendMessage("This command can only be run by a player!");
            return true;
        } catch (RuntimeException ex) {
            sender.sendMessage("Command fail: " + ex.getMessage());
            ex.printStackTrace();
            return true;
        }

        return true;

//        TODO set infernal spawner
//        if (args[0].equals("setInfernal") && args.length == 2) {
//            if (player.getTargetBlock((Set<Material>) null, 25).getType().equals((Object) Material.MOB_SPAWNER)) {
//                final int delay = Integer.parseInt(args[1]);
//                Location loc = player.getTargetBlock((Set<Material>) null, 25).getLocation();
//                plugin.persist.validInfernalSpawners.put(loc, delay);
//                sender.sendMessage("Spawner set to infernal with a " + delay + " second delay!");
//            } else {
//                sender.sendMessage("You must be looking a spawner to make it infernal!");
//            }
//        }
    }

    private String getLootDisplayName(String k) {
        if (plugin.lootManager.cfg.lootItems.containsKey(k)) {
            ItemStack s = plugin.lootManager.cfg.lootItems.get(k).item;
            if (s != null && s.hasItemMeta() && s.getItemMeta().hasDisplayName()) {
                return s.getItemMeta().getDisplayName();
            } else {
                return k;
            }
        } else {
            return k;
        }
    }

    private <K> Map<K,Double> normalize(Map<K, Double> v) {
        Double sum = v.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<K, Double> ret = new HashMap<>();
        for (Map.Entry<K, Double> e : v.entrySet())
            ret.put(e.getKey(), e.getValue() / sum);
        return ret;
    }

    public static void printHelp(final CommandSender sender) {
        sender.sendMessage("--Infernal Mobs v" + Bukkit.getServer().getPluginManager().getPlugin("InfernalMobs").getDescription().getVersion() + "--");
        sender.sendMessage("Usage: /im reloadMainLoopTask");
        sender.sendMessage("Usage: /im worldInfo");
        sender.sendMessage("Usage: /im error");
        sender.sendMessage("Usage: /im getloot <index>");
        sender.sendMessage("Usage: /im abilities");
        sender.sendMessage("Usage: /im setInfernal <time delay>");
        sender.sendMessage("Usage: /im spawn <mob> <ability> <ability>");
        sender.sendMessage("Usage: /im cspawn <mob> <world> <x> <y> <z> <ability> <ability>");
        sender.sendMessage("Usage: /im pspawn <mob> <player> <ability> <ability>");
        sender.sendMessage("Usage: /im kill <size>");
        sender.sendMessage("Usage: /im killall <world>");
        sender.sendMessage("Usage: /im addloot <name>");
        sender.sendMessage("Usage: /im setdrop <level> <name> <weight>");
        sender.sendMessage("Usage: /im checkchance level <level>");
        sender.sendMessage("Usage: /im checkchance item <itemName>");
    }

    public static void printAbilities(CommandSender sender) {
        sender.sendMessage("--Infernal Mobs Abilities--");
        List<String> l = new ArrayList<>();
        for (EnumAbilities a : EnumAbilities.values()) {
            l.add(a.name().toLowerCase());
        }
        l.sort(Comparator.naturalOrder());
        for (String str : l) {
            sender.sendMessage("  - " + str);
        }
    }
}
