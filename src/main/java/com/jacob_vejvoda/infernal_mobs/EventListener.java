package com.jacob_vejvoda.infernal_mobs;

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
                final Block spawner = EventListener.plugin.blockNear(event.getEntity().getLocation(), Material.MOB_SPAWNER, 10);
                if (spawner != null) {
                    final String name = EventListener.plugin.getLocationName(spawner.getLocation());
                    if (EventListener.plugin.mobSaveFile.getString("infernalSpanwers." + name) != null) {
                        if (this.spawnerMap.get(name) == null) {
                            EventListener.plugin.makeInfernal((Entity) event.getEntity(), true);
                            this.spawnerMap.put(name, EventListener.plugin.serverTime);
                        } else {
                            final long startTime = this.spawnerMap.get(name);
                            final long endTime = EventListener.plugin.serverTime;
                            final long timePassed = endTime - startTime;
                            final int delay = EventListener.plugin.mobSaveFile.getInt("infernalSpanwers." + name);
                            if (timePassed >= delay) {
                                EventListener.plugin.makeInfernal((Entity) event.getEntity(), true);
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
                EventListener.plugin.makeInfernal((Entity) event.getEntity(), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent e) throws IOException {
        if (e.getBlock().getType().equals((Object) Material.MOB_SPAWNER)) {
            final String name = EventListener.plugin.getLocationName(e.getBlock().getLocation());
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
                    final TNTPrimed tnt = (TNTPrimed) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(1);
                }
                boolean isGhost = false;
                try {
                    if (((Zombie) event.getEntity()).getEquipment().getHelmet().getItemMeta().getDisplayName().equals("§fGhost Head")) {
                        isGhost = true;
                    }
                } catch (Exception ex) {
                }
                if (aList.contains("ghost")) {
                    EventListener.plugin.spawnGhost(event.getEntity().getLocation());
                }
                Location dropSpot = null;
                if (aList.contains("molten")) {
                    final Location lavaSpot = dropSpot = event.getEntity().getLocation();
                    dropSpot.setX(dropSpot.getX() - 2.0);
                } else {
                    dropSpot = event.getEntity().getLocation();
                }
                if (EventListener.plugin.getConfig().getBoolean("enableDeathMessages") && event.getEntity().getKiller() instanceof Player && !isGhost) {
                    final Player player = event.getEntity().getKiller();
                    if (EventListener.plugin.getConfig().getList("deathMessages") != null) {
                        final ArrayList<String> deathMessagesList = (ArrayList<String>) EventListener.plugin.getConfig().getList("deathMessages");
                        final Random randomGenerator = new Random();
                        final int index = randomGenerator.nextInt(deathMessagesList.size());
                        String deathMessage = deathMessagesList.get(index);
                        final String tittle = EventListener.plugin.gui.getMobNameTag((Entity) event.getEntity());
                        deathMessage = ChatColor.translateAlternateColorCodes('&', deathMessage);
                        deathMessage = deathMessage.replace("player", player.getName());
                        if (player.getItemInHand() != null && !player.getItemInHand().getType().equals((Object) Material.AIR)) {
                            if (player.getItemInHand().getItemMeta().getDisplayName() != null) {
                                deathMessage = deathMessage.replace("weapon", player.getItemInHand().getItemMeta().getDisplayName());
                            } else {
                                deathMessage = deathMessage.replace("weapon", player.getItemInHand().getType().name().replace("_", " ").toLowerCase());
                            }
                        } else {
                            deathMessage = deathMessage.replace("weapon", "fist");
                        }
                        if (event.getEntity().getCustomName() != null) {
                            deathMessage = deathMessage.replace("mob", event.getEntity().getCustomName());
                        } else {
                            deathMessage = deathMessage.replace("mob", tittle);
                        }
                        Bukkit.broadcastMessage(deathMessage);
                    } else {
                        System.out.println("No valid death messages found!");
                    }
                }
                if (EventListener.plugin.getConfig().getBoolean("enableDrops") && (EventListener.plugin.getConfig().getBoolean("enableFarmingDrops") || event.getEntity().getKiller() != null) && (EventListener.plugin.getConfig().getBoolean("enableFarmingDrops") || event.getEntity().getKiller() instanceof Player)) {
                    Player player = null;
                    if (event.getEntity().getKiller() instanceof Player) {
                        player = event.getEntity().getKiller();
                    }
                    if (player != null && player.getGameMode().equals((Object) GameMode.CREATIVE) && EventListener.plugin.getConfig().getBoolean("noCreativeDrops")) {
                        return;
                    }
                    final ItemStack drop = EventListener.plugin.getRandomLoot(player, event.getEntity().getType().getName(), aList.size());
                    if (drop != null) {
                        final int min = 1;
                        final int max = EventListener.plugin.getConfig().getInt("dropChance");
                        final int randomNum = new Random().nextInt(max - min + 1) + min;
                        if (dropSpot != null && randomNum == 1) {
                            final Item dropedItem = event.getEntity().getWorld().dropItemNaturally(dropSpot, drop);
                            EventListener.plugin.keepAlive(dropedItem);
                        }
                        final int xpm = EventListener.plugin.getConfig().getInt("xpMultiplier");
                        final int xp = event.getDroppedExp() * xpm;
                        event.setDroppedExp(xp);
                    }
                }
                try {
                    EventListener.plugin.removeMob(mobIndex);
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
        } catch (Exception e2) {
            System.out.println("EntityDeathEvent: " + e2);
        }
    }
}
