package com.jacob_vejvoda.infernal_mobs;

import cat.nyaa.nyaacore.Message;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EventListener implements Listener {
    private final InfernalMobs plugin;

    public EventListener(final InfernalMobs instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        final Player p = e.getPlayer();
        final Entity ent = e.getRightClicked();
        if (p == null || !(ent instanceof LivingEntity)) return;
        if (this.plugin.errorList.contains(p)) {
            this.plugin.errorList.remove(p);
            p.sendMessage("§6Error report:");
            String name = ent.getCustomName();
            if (name == null) name = ent.getType().name();
            p.sendMessage("§eName: §f" + name);
            p.sendMessage(String.format("§eHealth: §f %.2f/%.2f", ((LivingEntity) ent).getHealth(), ((LivingEntity) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
            Mob m = this.plugin.mobManager.mobMap.get(ent.getUniqueId());
            if (m == null) {
                p.sendMessage("§eInfernal: §sfalse");
            } else {
                p.sendMessage("§eInfernal Abilities: §s" + m.abilityList);
                p.sendMessage("§eInfernal Lives: §s" + m.lives);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamaged(final EntityDamageEvent e) {
        final Entity mob = e.getEntity();
        if (this.plugin.mobManager.mobMap.containsKey(mob.getUniqueId())) {
            for (final Entity entity : mob.getNearbyEntities(64.0, 64.0, 64.0)) {
                if (entity instanceof Player) {
                    GUI.refreshPlayerScoreboard((Player)entity);
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
    public void onEntityAttack(final EntityDamageByEntityEvent event) {
        boolean isDirectAttack = true;
        Entity trueVictim = event.getEntity();
        Entity trueAttacker = event.getDamager();
        if (trueAttacker instanceof Projectile) {
            isDirectAttack = false;
            ProjectileSource src = ((Projectile) trueAttacker).getShooter();
            if (src instanceof Entity) {
                trueAttacker = (Entity) src;
            } else {
                return;
            }
        }
        if (!(trueAttacker instanceof LivingEntity)) return;
        if (!(trueVictim instanceof LivingEntity)) return;

        if (plugin.mobManager.mobMap.containsKey(trueVictim.getUniqueId())) {
            // something attacked infernal mob
            if (!(trueAttacker instanceof Player)) return;
            if (((Player) trueAttacker).getGameMode() == GameMode.CREATIVE) return;
            Mob mob = plugin.mobManager.mobMap.get(trueVictim.getUniqueId());
            for (EnumAbilities ab : mob.abilityList) {
                ab.onPlayerAttack((LivingEntity) trueVictim, mob, (Player) trueAttacker, isDirectAttack, event);
            }
        }

        if ((plugin.mobManager.mobMap.containsKey(trueAttacker.getUniqueId()))) {
            // infernal mob attacked something
            if (!(trueVictim instanceof Player)) return;
            if (((Player) trueVictim).getGameMode() == GameMode.CREATIVE) return;
            Mob mob = plugin.mobManager.mobMap.get(trueAttacker.getUniqueId());
            for (EnumAbilities ab : mob.abilityList) {
                ab.onAttackPlayer((LivingEntity) trueAttacker, mob, (Player) trueVictim, isDirectAttack, event);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMobSpawn(final CreatureSpawnEvent event) {
        LivingEntity e = event.getEntity();
        if (e.hasMetadata("NPC")) return;
        if (e.hasMetadata("shopkeeper")) return;
        if (e.getCustomName() != null) return;
        if (!ConfigReader.isEnabledWorld(e.getWorld())) return;
        if (!ConfigReader.isEnabledMobType(e.getType())) return;
        if (e.getLocation().getY() < ConfigReader.getNaturalSpawnMinHeight()) return;
        if (!ConfigReader.getEnabledSpawnReasons().contains(event.getSpawnReason())) return;
        new BukkitRunnable(){
            @Override
            public void run() {
                plugin.mobManager.infernalNaturalSpawn(e);
            }
        }.runTaskLater(plugin, 10L);
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

    private static boolean determineShouldDrop(boolean killedByPlayer, boolean isCreativePlayer) {
        if (!ConfigReader.isDropEnabled()) return false;
        if (killedByPlayer) {
            return isCreativePlayer ? ConfigReader.isCreativeDropEnabled() : true;
        } else {
            return ConfigReader.isFarmingDropEnabled();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(final EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        Mob mob = plugin.mobManager.mobMap.get(id);
        if (mob == null) return;
        LivingEntity mobEntity = event.getEntity();

        for (EnumAbilities ab : mob.abilityList) {
            ab.onDeath(mobEntity, mob, mobEntity.getKiller(), event);
        }

        // item drop decision
        ItemStack selectedDropItem = null;
        Player killer = mobEntity.getKiller();
        if (determineShouldDrop(killer != null, (killer!=null) && (killer.getGameMode()==GameMode.CREATIVE))) {
            ItemStack drop = this.plugin.lootManager.getRandomLoot(killer, mob.getMobLevel());
            if (drop != null) {
                final int min = 1;
                final int max = ConfigReader.getDropChance();
                final int randomNum = new Random().nextInt(max - min + 1) + min;
                if (randomNum == 1) {
                    event.getDrops().add(drop);
                    selectedDropItem = drop;
                }
            }
        }

        // set xp drop
        final int xpm = ConfigReader.getXpMultiplier();
        final int xp = event.getDroppedExp() * xpm;
        event.setDroppedExp(xp);

        // broadcast death message TODO use ConfigReader
        if (ConfigReader.isMobDeathMessageEnabled() && event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            String playerName = player.getName();
            String mobName;
            if (event.getEntity().getCustomName() != null) {
                mobName = event.getEntity().getCustomName();
            } else {
                mobName = event.getEntity().getType().name();
            }
            boolean broadcastToAllWorld = ConfigReader.isDeathMessageBroadcastAllWorld();

            if (this.plugin.getConfig().getList("deathMessages") != null) {
                String deathMessage = Helper.randomItem(plugin.getConfig().getStringList("deathMessages"));
                deathMessage = ChatColor.translateAlternateColorCodes('&', deathMessage);
                deathMessage = deathMessage.replace("{player}", playerName);
                deathMessage = deathMessage.replace("{mob}", mobName);
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null && !item.getType().equals(Material.AIR)) {
                    if (broadcastToAllWorld) {
                        new Message("")
                                .append(deathMessage, item)
                                .broadcast();
                    } else {
                        new Message("")
                                .append(deathMessage, item)
                                .broadcast(player.getLocation().getWorld());
                    }
                } else {
                    if (broadcastToAllWorld) {
                        new Message(
                                deathMessage.replace("{itemName}", "fist").replace("{itemName:0}", "fist")
                        ).broadcast();
                    } else {
                        new Message(
                                deathMessage.replace("{itemName}", "fist").replace("{itemName:0}", "fist")
                        ).broadcast(player.getLocation().getWorld());
                    }
                }
            } else {
                System.out.println("No valid death messages found!");
            }

            if (plugin.getConfig().isList("dropMessages") && selectedDropItem != null) {
                String msg = Helper.randomItem(plugin.getConfig().getStringList("dropMessages"));
                msg = ChatColor.translateAlternateColorCodes('&', msg);
                msg = msg.replace("{player}", playerName);
                msg = msg.replace("{mob}", mobName);
                if (broadcastToAllWorld) {
                    new Message("")
                            .append(msg, selectedDropItem)
                            .broadcast();
                } else {
                    new Message("")
                            .append(msg, selectedDropItem)
                            .broadcast(player.getLocation().getWorld());
                }
            }

            if (plugin.getConfig().isList("nodropMessages") && selectedDropItem == null) {
                String msg = Helper.randomItem(plugin.getConfig().getStringList("nodropMessages"));
                msg = ChatColor.translateAlternateColorCodes('&', msg);
                msg = msg.replace("{player}", playerName);
                msg = msg.replace("{mob}", mobName);
                if (broadcastToAllWorld) {
                    new Message(msg)
                            .broadcast();
                } else {
                    new Message(msg)
                            .broadcast(player.getLocation().getWorld());
                }
            }
        }

        plugin.mobManager.mobMap.remove(id);

        // TODO event
    }
}
