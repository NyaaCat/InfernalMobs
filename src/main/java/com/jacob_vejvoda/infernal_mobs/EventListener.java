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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public class EventListener implements Listener {
    private final infernal_mobs plugin;
    private Map<Location, Long> spawnerLastSpawnTime;

    public EventListener(final infernal_mobs instance) {
        this.spawnerLastSpawnTime = new HashMap<>();
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        final Player p = e.getPlayer();
        final Entity ent = e.getRightClicked();
        if (this.plugin.errorList.contains(p)) {
            this.plugin.errorList.remove(p);
            p.sendMessage("§6Error report:");
            String name = "";
            try {
                name = ent.getCustomName();
            } catch (Exception ex) {
            }
            p.sendMessage("§eName: §f" + name);
            p.sendMessage("§eHealth: §f" + ((Damageable) ent).getMaxHealth());
            p.sendMessage("§eInfernal: §s" + this.plugin.mobManager.mobMap.containsKey(ent.getUniqueId()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnitityDamaged(final EntityDamageEvent e) {
        final Entity mob = e.getEntity();
        if (this.plugin.mobManager.mobMap.containsKey(mob.getUniqueId())) {
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
            if (this.plugin.mobManager.mobMap.containsKey(m.getUniqueId())) {
                e.setCancelled(true);
                break;
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
                    this.plugin.doEffect(player, mob, false);
                } else if (!(arrow.getShooter() instanceof Player) && victim instanceof Player) {
                    final Entity mob = (Entity) arrow.getShooter();
                    final Player player = (Player) victim;
                    this.plugin.doEffect(player, mob, true);
                }
            } else if (attacker instanceof Snowball) {
                final Snowball snowBall = (Snowball) event.getDamager();
                if (snowBall.getShooter() != null) {
                    if (snowBall.getShooter() instanceof Player && !(victim instanceof Player)) {
                        final Entity mob = victim;
                        final Player player = (Player) snowBall.getShooter();
                        this.plugin.doEffect(player, mob, false);
                    } else if (!(snowBall.getShooter() instanceof Player) && victim instanceof Player) {
                        final Entity mob = (Entity) snowBall.getShooter();
                        final Player player = (Player) victim;
                        this.plugin.doEffect(player, mob, true);
                    }
                }
            } else if (attacker instanceof Player && !(victim instanceof Player)) {
                final Player player2 = (Player) attacker;
                final Entity mob = victim;
                this.plugin.doEffect(player2, mob, false);
            } else if (!(attacker instanceof Player) && victim instanceof Player) {
                final Player player2 = (Player) victim;
                final Entity mob = attacker;
                this.plugin.doEffect(player2, mob, true);
            }
            if (this.plugin.mobManager.mobMap.containsKey(victim.getUniqueId())) {
                for (final Entity entity : victim.getNearbyEntities(64.0, 64.0, 64.0)) {
                    if (entity instanceof Player) {
                        GUI.fixBar((Player) entity);
                    }
                }
            }
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMobSpawn(final CreatureSpawnEvent event) {
        Entity e = event.getEntity();
        if (e.hasMetadata("NPC") || e.hasMetadata("shopkeeper") || e.getCustomName() != null) {
            return;
        }

        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            Block spawner = Helper.blockNear(event.getEntity().getLocation(), Material.MOB_SPAWNER, 10);
            if (spawner == null) return;
            Location spawnerLocation = spawner.getLocation();
            if (!plugin.persist.validInfernalSpawners.containsKey(spawnerLocation)) return;
            long startTime = this.spawnerLastSpawnTime.get(spawnerLocation);
            long endTime = System.currentTimeMillis();
            final long timePassed = endTime - startTime;
            final int delay = plugin.persist.validInfernalSpawners.get(spawnerLocation);
            if (timePassed >= delay) {
                plugin.mobManager.makeInfernal(e, true);
                this.spawnerLastSpawnTime.put(spawnerLocation, endTime);
            } else {
                event.setCancelled(true);
            }
        } else {
            World world = e.getWorld();
            String typeName = e.getType().name();
            if ((this.plugin.getConfig().getList("enabledworlds").contains(world.getName()) || this.plugin.getConfig().getList("enabledworlds").contains("<all>")) &&
                    this.plugin.getConfig().getList("enabledmobs").contains(typeName) &&
                    this.plugin.getConfig().getInt("naturalSpawnHeight") < e.getLocation().getY() &&
                    this.plugin.getConfig().getList("enabledSpawnReasons").contains(event.getSpawnReason().toString())) {
                plugin.mobManager.makeInfernal(e, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (e.getBlock().getType().equals(Material.MOB_SPAWNER)) {
            Location loc = e.getBlock().getLocation();
            if (plugin.persist.validInfernalSpawners.containsKey(loc)) {
                plugin.persist.validInfernalSpawners.remove(loc);
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
            Mob m = plugin.mobManager.mobMap.get(id);
            if (m == null) return;
            final List<String> aList = m.abilityList;
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
                this.plugin.mobManager.spawnGhost(event.getEntity().getLocation());
            }

            // item drop decision
            ItemStack selectedDropItem = null;
            if (this.plugin.getConfig().getBoolean("enableDrops") && (this.plugin.getConfig().getBoolean("enableFarmingDrops") || event.getEntity().getKiller() != null) && (this.plugin.getConfig().getBoolean("enableFarmingDrops") || event.getEntity().getKiller() instanceof Player)) {
                Player player = null;
                if (event.getEntity().getKiller() instanceof Player) {
                    player = event.getEntity().getKiller();
                }
                if (player != null && player.getGameMode().equals((Object) GameMode.CREATIVE) && this.plugin.getConfig().getBoolean("noCreativeDrops")) {

                } else {
                    final ItemStack drop = this.plugin.getRandomLoot(player, event.getEntity().getType().getName(), aList.size());
                    if (drop != null) {
                        final int min = 1;
                        final int max = this.plugin.getConfig().getInt("dropChance");
                        final int randomNum = new Random().nextInt(max - min + 1) + min;
                        if (randomNum == 1) {
                            event.getDrops().add(drop);
                            selectedDropItem = drop;
                        }
                        final int xpm = this.plugin.getConfig().getInt("xpMultiplier");
                        final int xp = event.getDroppedExp() * xpm;
                        event.setDroppedExp(xp);
                    }
                }
            }
            try {
                this.plugin.removeMob(id);
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

            // print death message
            if (this.plugin.getConfig().getBoolean("enableDeathMessages") && event.getEntity().getKiller() != null && !isGhost) {
                Player player = event.getEntity().getKiller();
                String playerName = player.getName();
                String mobName;
                if (event.getEntity().getCustomName() != null) {
                    mobName = event.getEntity().getCustomName();
                } else {
                    mobName = event.getEntity().getType().name();
                }

                if (this.plugin.getConfig().getList("deathMessages") != null) {
                    String deathMessage = Helper.randomItem(plugin.getConfig().getStringList("deathMessages"));
                    deathMessage = ChatColor.translateAlternateColorCodes('&', deathMessage);
                    deathMessage = deathMessage.replace("{player}", playerName);
                    deathMessage = deathMessage.replace("{mob}", mobName);
                    if (player.getItemInHand() != null && !player.getItemInHand().getType().equals(Material.AIR)) {
                        new Message("").append(deathMessage, player.getItemInHand()).broadcast();
                    } else {
                        Bukkit.broadcastMessage(deathMessage.replace("{itemName}", "fist").replace("{itemName:0}", "fist"));
                    }
                } else {
                    System.out.println("No valid death messages found!");
                }

                if (plugin.getConfig().isList("dropMessages") && selectedDropItem != null) {
                    String msg = Helper.randomItem(plugin.getConfig().getStringList("dropMessages"));
                    msg = ChatColor.translateAlternateColorCodes('&', msg);
                    msg = msg.replace("{player}", playerName);
                    msg = msg.replace("{mob}", mobName);
                    new Message("").append(msg, selectedDropItem).broadcast();
                }

                if (plugin.getConfig().isList("nodropMessages") && selectedDropItem == null) {
                    String msg = Helper.randomItem(plugin.getConfig().getStringList("nodropMessages"));
                    msg = ChatColor.translateAlternateColorCodes('&', msg);
                    msg = msg.replace("{player}", playerName);
                    msg = msg.replace("{mob}", mobName);
                    Bukkit.broadcastMessage(msg);
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            System.out.println("EntityDeathEvent: " + e2);
        }
    }
}
