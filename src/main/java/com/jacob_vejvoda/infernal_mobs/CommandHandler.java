package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class CommandHandler implements CommandExecutor {
    private final infernal_mobs plugin;

    public CommandHandler(infernal_mobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!cmd.getName().equals("infernalmobs")) {
            if (!cmd.getName().equals("im")) {
                return true;
            }
        }
        try {
            Player player = null;
            boolean isPlayer = sender instanceof Player;
            if (isPlayer) player = (Player) sender;
            if (!sender.hasPermission("infernal_mobs.commands")) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            if (args.length <= 0) {
                throwError(sender);
                return true;
            }

            if (!isPlayer && !args[0].equals("cspawn") && !args[0].equals("reload") && !args[0].equals("killall")) {
                sender.sendMessage("This command can only be run by a player!");
                return true;
            }

            if (args[0].equals("reload")) {
                plugin.reloadConfig();
                plugin.reloadLoot();
                sender.sendMessage("§eConfig reloaded!");
            } else if (args[0].equals("mobList")) {
                sender.sendMessage("§6Mob List:");
                EntityType[] values;
                for (int length = (values = EntityType.values()).length, n = 0; n < length; ++n) {
                    final EntityType et = values[n];
                    if (et != null && et.getName() != null) {
                        sender.sendMessage("§e" + et.getName());
                    }
                }
            } else if (args[0].equals("error")) {
                plugin.errorList.add(player);
                sender.sendMessage("§eClick on a mob to send an error report about it.");
            } else if (args[0].equals("info")) {
                sender.sendMessage("§eMounts: " + plugin.mountList.size());
                sender.sendMessage("§eInfernals: " + plugin.infernalList.size());
            } else if (args[0].equals("worldInfo")) {
                final ArrayList<String> enWorldList = (ArrayList<String>) plugin.getConfig().getList("enabledworlds");
                final World world = player.getWorld();
                String enabled = "is not";
                if (enWorldList.contains(world.getName()) || enWorldList.contains("<all>")) {
                    enabled = "is";
                }
                sender.sendMessage("The world you are currently in, " + world + " " + enabled + " enabled.");
                sender.sendMessage("All the world that are enabled are: " + enWorldList.toString());
            } else if (args[0].equals("help")) {
                throwError(sender);
            } else if (args[0].equals("getloot")) {
                final int min = plugin.getConfig().getInt("minpowers");
                final int max = plugin.getConfig().getInt("maxpowers");
                final int powers = Helper.rand(min, max);
                final ItemStack gottenLoot = plugin.getRandomLoot(player, plugin.getRandomMob(), powers);
                if (gottenLoot != null) {
                    player.getInventory().addItem(new ItemStack[]{gottenLoot});
                }
                sender.sendMessage("§eGave you some random loot!");
            } else if (args.length == 2 && args[0].equals("getloot")) {
                try {
                    final int index = Integer.parseInt(args[1]);
                    final ItemStack i = plugin.getLoot(player, index);
                    if (i != null) {
                        player.getInventory().addItem(new ItemStack[]{i});
                        sender.sendMessage("§eGave you the loot at index §9" + index);
                        return true;
                    }
                } catch (Exception ex) {
                }
                sender.sendMessage("§cUnable to get that loot!");
            } else if ((args.length >= 2 && args[0].equals("spawn"))) { // spawn at cursor
                EntityType type = EntityType.fromName(args[1]);
                if (type == null) {
                    sender.sendMessage("Can't spawn a " + args[1] + "!");
                    return true;
                }
                Location farSpawnLoc = player.getTargetBlock((Set<Material>) null, 200).getLocation();
                farSpawnLoc.setY(farSpawnLoc.getY() + 1.0);
                ArrayList<String> abList;
                if (args.length == 2) { // determine level by distance
                    abList = plugin.getAbilitiesAmount(farSpawnLoc);
                } else { // manually input abilities
                    abList = new ArrayList<>();
                    for (int j = 0; j <= args.length - 3; ++j) {
                        if (plugin.getConfig().getString(args[j + 2]) == null) {
                            sender.sendMessage(String.valueOf(args[j + 2]) + " is not a valid ability!");
                            return true;
                        }
                        abList.add(args[j + 2]);
                    }
                }
                Mob mob = plugin.mobManager.spawnMob(type, farSpawnLoc, abList);
                if (mob != null) {
                    sender.sendMessage("Spawned a " + args[1]);
                } else {
                    sender.sendMessage("Cannot spawn " + args[1]);
                }
            } else if ((args.length >= 6 && args[0].equals("cspawn"))) { // spawn at world,x,y,z
                EntityType type = EntityType.fromName(args[1]);
                if (type == null) {
                    sender.sendMessage("Can't spawn a " + args[1] + "!");
                    return true;
                }
                if (Bukkit.getServer().getWorld(args[2]) == null) {
                    sender.sendMessage(String.valueOf(args[2]) + " dose not exist!");
                    return true;
                }
                final World world2 = Bukkit.getServer().getWorld(args[2]);
                final Location farSpawnLoc = new Location(world2, (double) Integer.parseInt(args[3]), (double) Integer.parseInt(args[4]), (double) Integer.parseInt(args[5]));

                ArrayList<String> abList;
                if (args.length == 6) { // determine level by distance
                    abList = plugin.getAbilitiesAmount(farSpawnLoc);
                } else { // manually input abilities
                    abList = new ArrayList<>();
                    for (int j = 0; j <= args.length - 7; ++j) {
                        if (plugin.getConfig().getString(args[j + 6]) == null) {
                            sender.sendMessage(String.valueOf(args[j + 6]) + " is not a valid ability!");
                            return true;
                        }
                        abList.add(args[j + 6]);
                    }
                }
                Mob mob = plugin.mobManager.spawnMob(type, farSpawnLoc, abList);
                if (mob != null) {
                    sender.sendMessage("Spawned a " + args[1]);
                } else {
                    sender.sendMessage("Cannot spawn " + args[1]);
                }
            } else if (args[0].equals("pspawn") && args.length >= 3) { // spawn at player
                EntityType type = EntityType.fromName(args[1]);
                if (type == null) {
                    sender.sendMessage("Can't spawn a " + args[1] + "!");
                    return true;
                }
                final Player p = plugin.getServer().getPlayer(args[2]);
                if (p == null) {
                    sender.sendMessage(String.valueOf(args[2]) + " is not online!");
                    return true;
                }
                Location farSpawnLoc = p.getLocation();
                ArrayList<String> abList;
                if (args.length == 3) { // determine level by distance
                    abList = plugin.getAbilitiesAmount(farSpawnLoc);
                } else { // manually input abilities
                    abList = new ArrayList<>();
                    for (int j = 0; j <= args.length - 4; ++j) {
                        if (plugin.getConfig().getString(args[j + 3]) == null) {
                            sender.sendMessage(String.valueOf(args[j + 3]) + " is not a valid ability!");
                            return true;
                        }
                        abList.add(args[j + 3]);
                    }
                }
                Mob mob = plugin.mobManager.spawnMob(type, farSpawnLoc, abList);
                if (mob != null) {
                    sender.sendMessage("Spawned a " + args[1]);
                } else {
                    sender.sendMessage("Cannot spawn " + args[1]);
                }
            } else if (args.length == 1 && args[0].equals("abilities")) {
                sender.sendMessage("--Infernal Mobs Abilities--");
                sender.sendMessage("mama, molten, weakness, vengeance, webber, storm, sprint, "
                        + "lifesteal, ghastly, ender, cloaked, berserk, 1up, sapper, rust, bullwark, "
                        + "quicksand, thief, tosser, withering, blinding, armoured, poisonous, potions, "
                        + "explode, gravity, archer, necromancer, firework, flying, mounted, morph, ghost, confusing");
            } else if (args.length == 1 && args[0].equals("showAbilities")) {
                if (plugin.getTarget(player) != null) {
                    final Entity targeted = plugin.getTarget(player);
                    final UUID mobId = targeted.getUniqueId();
                    if (plugin.idSearch(mobId) != -1) {
                        final ArrayList<String> oldMobAbilityList = plugin.findMobAbilities(mobId);
                        if (!targeted.isDead()) {
                            sender.sendMessage("--Targeted Mob's Abilities--");
                            sender.sendMessage(oldMobAbilityList.toString());
                        }
                    } else {
                        sender.sendMessage("§cThis " + targeted.getType().getName() + " §cis not an infernal mob!");
                    }
                } else {
                    sender.sendMessage("§cUnable to find mob!");
                }
            } else if (args[0].equals("setInfernal") && args.length == 2) {
                if (player.getTargetBlock((Set<Material>) null, 25).getType().equals((Object) Material.MOB_SPAWNER)) {
                    final int delay = Integer.parseInt(args[1]);
                    final String name = Helper.getLocationName(player.getTargetBlock((Set<Material>) null, 25).getLocation());
                    plugin.mobSaveFile.set("infernalSpanwers." + name, (Object) delay);
                    plugin.mobSaveFile.save(plugin.saveYML);
                    sender.sendMessage("§cSpawner set to infernal with a " + delay + " second delay!");
                } else {
                    sender.sendMessage("§cYou must be looking a spawner to make it infernal!");
                }
            } else if (args[0].equals("kill") && args.length == 2) {
                final int size = Integer.parseInt(args[1]);
                for (final Entity e : player.getNearbyEntities((double) size, (double) size, (double) size)) {
                    final int id2 = plugin.idSearch(e.getUniqueId());
                    if (id2 != -1) {
                        plugin.removeMob(id2);
                        e.remove();
                        plugin.getLogger().log(Level.INFO, "Entity remove due to /kill");
                    }
                }
                sender.sendMessage("§eKilled all infernal mobs near you!");
            } else if (args[0].equals("killall") && args.length == 2) {
                final World w = plugin.getServer().getWorld(args[1]);
                if (w != null) {
                    for (final Entity e : w.getEntities()) {
                        final int id2 = plugin.idSearch(e.getUniqueId());
                        if (id2 != -1) {
                            plugin.removeMob(id2);
                            plugin.getLogger().log(Level.INFO, "Entity remove due to /killall");
                            e.remove();
                        }
                    }
                    sender.sendMessage("§eKilled all loaded infernal mobs in that world!");
                } else {
                    sender.sendMessage("§cWorld not found!");
                }
            } else {
                throwError(sender);
            }
        } catch (Exception x) {
            throwError(sender);
            x.printStackTrace();
        }
        return true;
    }

    public static void throwError(final CommandSender sender) {
        sender.sendMessage("--Infernal Mobs v" + Bukkit.getServer().getPluginManager().getPlugin("InfernalMobs").getDescription().getVersion() + "--");
        sender.sendMessage("Usage: /im reload");
        sender.sendMessage("Usage: /im worldInfo");
        sender.sendMessage("Usage: /im error");
        sender.sendMessage("Usage: /im getloot <index>");
        sender.sendMessage("Usage: /im abilities");
        sender.sendMessage("Usage: /im showAbilities");
        sender.sendMessage("Usage: /im setInfernal <time delay>");
        sender.sendMessage("Usage: /im spawn <mob> <ability> <ability>");
        sender.sendMessage("Usage: /im cspawn <mob> <world> <x> <y> <z> <ability> <ability>");
        sender.sendMessage("Usage: /im pspawn <mob> <player> <ability> <ability>");
        sender.sendMessage("Usage: /im kill <size>");
        sender.sendMessage("Usage: /im killall <world>");
    }
}
