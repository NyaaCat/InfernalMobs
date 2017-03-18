package com.jacob_vejvoda.infernal_mobs;

import cat.nyaa.utils.Message;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class EventListener implements Listener {
    static infernal_mobs plugin;
    HashMap<String, Long> spawnerMap;

    public EventListener(final infernal_mobs instance) {
        this.spawnerMap = new HashMap<String, Long>();
        EventListener.plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        final Player p = e.getPlayer();
        final Entity ent = e.getRightClicked();
        if (EventListener.plugin.errorList.contains(p)) {
            EventListener.plugin.errorList.remove(p);
            p.sendMessage("§6Error report:");
            String name = "";
            try {
                name = ent.getCustomName();
            } catch (Exception ex) {
            }
            p.sendMessage("§eName: §f" + name);
            p.sendMessage("§eSaved: §f" + EventListener.plugin.mobSaveFile.getString(ent.getUniqueId().toString()));
            p.sendMessage("§eHealth: §f" + ((Damageable) ent).getMaxHealth());
            p.sendMessage("§eInfernal: §f" + EventListener.plugin.idSearch(ent.getUniqueId()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnitityDamaged(final EntityDamageEvent e) {
        final Entity mob = e.getEntity();
        if (EventListener.plugin.idSearch(mob.getUniqueId()) != -1) {
            for (final Entity entity : mob.getNearbyEntities(64.0, 64.0, 64.0)) {
                if (entity instanceof Player) {
                    GUI.fixBar((Player) entity);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLightningStrike(final LightningStrikeEvent e) {
        for (final Entity m : e.getLightning().getNearbyEntities(6.0, 6.0, 6.0)) {
            if (EventListener.plugin.idSearch(m.getUniqueId()) != -1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        final World world = event.getPlayer().getWorld();
        EventListener.plugin.giveMobsPowers(world);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        final World world = event.getPlayer().getWorld();
        EventListener.plugin.giveMobsPowers(world);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(final ChunkLoadEvent e) throws Exception {
        Entity[] entities;
        for (int length = (entities = e.getChunk().getEntities()).length, i = 0; i < length; ++i) {
            final Entity ent = entities[i];
            if (ent instanceof LivingEntity && ((LivingEntity) ent).getCustomName() != null && EventListener.plugin.mobSaveFile.getString(ent.getUniqueId().toString()) != null) {
                EventListener.plugin.giveMobPowers(ent);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(final ChunkUnloadEvent e) throws Exception {
        Entity[] entities;
        for (int length = (entities = e.getChunk().getEntities()).length, i = 0; i < length; ++i) {
            final Entity ent = entities[i];
            final int s = EventListener.plugin.idSearch(ent.getUniqueId());
            if (s != -1) {
                EventListener.plugin.infernalList.remove(EventListener.plugin.infernalList.get(s));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityAttack(final EntityDamageByEntityEvent event) throws Exception {
        try {
            final Entity attacker = event.getDamager();
            final Entity victim = event.getEntity();
            if (attacker instanceof Arrow) {
                final Arrow arrow = (Arrow) event.getDamager();
                if (arrow.getShooter() instanceof Player && !(victim instanceof Player)) {
                    final Entity mob = victim;
                    final Player player = (Player) arrow.getShooter();
                    EventListener.plugin.doEffect(player, mob, false);
                } else if (!(arrow.getShooter() instanceof Player) && victim instanceof Player) {
                    final Entity mob = (Entity) arrow.getShooter();
                    final Player player = (Player) victim;
                    EventListener.plugin.doEffect(player, mob, true);
                }
            } else if (attacker instanceof Snowball) {
                final Snowball snowBall = (Snowball) event.getDamager();
                if (snowBall.getShooter() != null) {
                    if (snowBall.getShooter() instanceof Player && !(victim instanceof Player)) {
                        final Entity mob = victim;
                        final Player player = (Player) snowBall.getShooter();
                        EventListener.plugin.doEffect(player, mob, false);
                    } else if (!(snowBall.getShooter() instanceof Player) && victim instanceof Player) {
                        final Entity mob = (Entity) snowBall.getShooter();
                        final Player player = (Player) victim;
                        EventListener.plugin.doEffect(player, mob, true);
                    }
                }
            } else if (attacker instanceof Player && !(victim instanceof Player)) {
                final Player player2 = (Player) attacker;
                final Entity mob = victim;
                EventListener.plugin.doEffect(player2, mob, false);
            } else if (!(attacker instanceof Player) && victim instanceof Player) {
                final Player player2 = (Player) victim;
                final Entity mob = attacker;
                EventListener.plugin.doEffect(player2, mob, true);
            }
            if (EventListener.plugin.idSearch(victim.getUniqueId()) != -1) {
                for (final Entity entity : victim.getNearbyEntities(64.0, 64.0, 64.0)) {
                    if (entity instanceof Player) {
                        GUI.fixBar((Player) entity);
                    }
                }
            }
        } catch (Exception e) {
            EventListener.plugin.getLogger().log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMobSpawn(final CreatureSpawnEvent event) {
        final World world = event.getEntity().getWorld();
        if (!event.getEntity().hasMetadata("NPC") && !event.getEntity().hasMetadata("shopkeeper") && event.getEntity().getCustomName() == null) {
            if (event.getSpawnReason().equals((Object) CreatureSpawnEvent.SpawnReason.SPAWNER)) {
                final Block spawner = Helper.blockNear(event.getEntity().getLocation(), Material.MOB_SPAWNER, 10);
                if (spawner != null) {
                    final String name = Helper.getLocationName(spawner.getLocation());
                    if (EventListener.plugin.mobSaveFile.getString("infernalSpanwers." + name) != null) {
                        if (this.spawnerMap.get(name) == null) {
                            plugin.mobManager.makeInfernal((Entity) event.getEntity(), true);
                            this.spawnerMap.put(name, EventListener.plugin.serverTime);
                        } else {
                            final long startTime = this.spawnerMap.get(name);
                            final long endTime = EventListener.plugin.serverTime;
                            final long timePassed = endTime - startTime;
                            final int delay = EventListener.plugin.mobSaveFile.getInt("infernalSpanwers." + name);
                            if (timePassed >= delay) {
                                plugin.mobManager.makeInfernal((Entity) event.getEntity(), true);
                                this.spawnerMap.put(name, EventListener.plugin.serverTime);
                            } else {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
            if (event.getEntity().hasMetadata("NPC") || event.getEntity().hasMetadata("shopkeeper")) {
                return;
            }
            String entName = event.getEntity().getType().name();
            if ((EventListener.plugin.getConfig().getList("enabledworlds").contains(world.getName()) || EventListener.plugin.getConfig().getList("enabledworlds").contains("<all>")) && EventListener.plugin.getConfig().getList("enabledmobs").contains(entName) && EventListener.plugin.getConfig().getInt("naturalSpawnHeight") < event.getEntity().getLocation().getY() && EventListener.plugin.getConfig().getList("enabledSpawnReasons").contains(event.getSpawnReason().toString())) {
                plugin.mobManager.makeInfernal((Entity) event.getEntity(), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent e) throws IOException {
        if (e.getBlock().getType().equals((Object) Material.MOB_SPAWNER)) {
            final String name = Helper.getLocationName(e.getBlock().getLocation());
            if (EventListener.plugin.mobSaveFile.getString("infernalSpanwers." + name) != null) {
                EventListener.plugin.mobSaveFile.set("infernalSpanwers." + name, (Object) null);
                EventListener.plugin.mobSaveFile.save(EventListener.plugin.saveYML);
                if (e.getPlayer().isOp()) {
                    e.getPlayer().sendMessage("§cYou broke an infernal mob spawner!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(final EntityDeathEvent event) {
        try {
            final UUID id = event.getEntity().getUniqueId();
            final int mobIndex = EventListener.plugin.idSearch(id);
            if (mobIndex != -1) {
                if (EventListener.plugin.findMobAbilities(id) == null) {
                    return;
                }
                final ArrayList<String> aList = EventListener.plugin.findMobAbilities(id);
                if (aList.contains("explode")) {
                    Location loc = event.getEntity().getLocation();
                    loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4, false, false);
                }
                boolean isGhost = false;
                try {
                    if (event.getEntity().getEquipment().getHelmet().getItemMeta().getDisplayName().equals("§fGhost Head")) {
                        isGhost = true;
                    }
                } catch (Exception ex) {
                }
                if (aList.contains("ghost")) {
                    EventListener.plugin.mobManager.spawnGhost(event.getEntity().getLocation());
                }

                // item drop decision
                ItemStack selectedDropItem = null;
                if (EventListener.plugin.getConfig().getBoolean("enableDrops") && (EventListener.plugin.getConfig().getBoolean("enableFarmingDrops") || event.getEntity().getKiller() != null) && (EventListener.plugin.getConfig().getBoolean("enableFarmingDrops") || event.getEntity().getKiller() instanceof Player)) {
                    Player player = null;
                    if (event.getEntity().getKiller() instanceof Player) {
                        player = event.getEntity().getKiller();
                    }
                    if (player != null && player.getGameMode().equals((Object) GameMode.CREATIVE) && EventListener.plugin.getConfig().getBoolean("noCreativeDrops")) {

                    } else {
                        final ItemStack drop = EventListener.plugin.getRandomLoot(player, event.getEntity().getType().getName(), aList.size());
                        if (drop != null) {
                            final int min = 1;
                            final int max = EventListener.plugin.getConfig().getInt("dropChance");
                            final int randomNum = new Random().nextInt(max - min + 1) + min;
                            if (randomNum == 1) {
                                event.getDrops().add(drop);
                                selectedDropItem = drop;
                            }
                            final int xpm = EventListener.plugin.getConfig().getInt("xpMultiplier");
                            final int xp = event.getDroppedExp() * xpm;
                            event.setDroppedExp(xp);
                        }
                    }
                }
                try {
                    EventListener.plugin.removeMob(mobIndex);
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }

                // print death message
                if (EventListener.plugin.getConfig().getBoolean("enableDeathMessages") && event.getEntity().getKiller() != null && !isGhost) {
                    Player player = event.getEntity().getKiller();
                    String playerName = player.getName();
                    String mobName;
                    if (event.getEntity().getCustomName() != null) {
                        mobName = event.getEntity().getCustomName();
                    } else {
                        mobName = event.getEntity().getType().name();
                    }

                    if (EventListener.plugin.getConfig().getList("deathMessages") != null) {
                        String deathMessage = Helper.randomItem(plugin.getConfig().getStringList("deathMessages"));
                        deathMessage = ChatColor.translateAlternateColorCodes('&', deathMessage);
                        deathMessage = deathMessage.replace("{player}", playerName);
                        deathMessage = deathMessage.replace("{mob}", mobName);
                        if (player.getItemInHand() != null && !player.getItemInHand().getType().equals(Material.AIR)) {
                            new Message(deathMessage).append(player.getItemInHand()).broadcast();
                        } else {
                            Bukkit.broadcastMessage(deathMessage + "fist");
                        }
                    } else {
                        System.out.println("No valid death messages found!");
                    }

                    if (plugin.getConfig().isList("dropMessages") && selectedDropItem != null) {
                        String msg = Helper.randomItem(plugin.getConfig().getStringList("dropMessages"));
                        msg = ChatColor.translateAlternateColorCodes('&', msg);
                        msg = msg.replace("{player}", playerName);
                        msg = msg.replace("{mob}", mobName);
                        new Message(msg).append(selectedDropItem).broadcast();
                    }

                    if (plugin.getConfig().isList("nodropMessages") && selectedDropItem == null) {
                        String msg = Helper.randomItem(plugin.getConfig().getStringList("nodropMessages"));
                        msg = ChatColor.translateAlternateColorCodes('&', msg);
                        msg = msg.replace("{player}", playerName);
                        msg = msg.replace("{mob}", mobName);
                        Bukkit.broadcastMessage(msg);
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            System.out.println("EntityDeathEvent: " + e2);
        }
    }
}
