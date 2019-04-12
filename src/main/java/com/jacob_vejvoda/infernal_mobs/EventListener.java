package com.jacob_vejvoda.infernal_mobs;

import cat.nyaa.nyaacore.Message;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.config.LevelConfig;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
                    GUI.refreshPlayerScoreboard((Player) entity);
//                    GUI.refreshBossBar((Player) entity);
                }
            }
            Mob im = this.plugin.mobManager.mobMap.get(mob.getUniqueId());
            Bukkit.getScheduler().runTask(InfernalMobs.instance, () -> BossBarManager.updateMob(im));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLightningStrike(final LightningStrikeEvent e) {
//        for (final Entity m : e.getLightning().getNearbyEntities(6.0, 6.0, 6.0)) {
//            if (this.plugin.mobManager.mobMap.containsKey(m.getUniqueId())) {
//                e.setCancelled(true);
//                break;
//            }
//        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityAttack(final EntityDamageByEntityEvent event) {
        //todo unexpected fireball damage
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
            Mob mob = plugin.mobManager.mobMap.get(trueVictim.getUniqueId());
            GameMode gameMode = ((Player) trueAttacker).getGameMode();
            if (gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR) {
                for (EnumAbilities ab : mob.abilityList) {
                    ab.onPlayerAttack((LivingEntity) trueVictim, mob, (Player) trueAttacker, isDirectAttack, event);
                }
            }
            if (ConfigReader.isEnhanceEnabled()) {
                double resistedDamage = ConfigReader.getLevelConfig().calcResistedDamage(event.getDamage(), mob.getMobLevel());
                event.setDamage(resistedDamage);
            }
        }

        if ((plugin.mobManager.mobMap.containsKey(trueAttacker.getUniqueId()))) {
            // infernal mob attacked something

            double originDamage = event.getDamage();
            Mob mob = plugin.mobManager.mobMap.get(trueAttacker.getUniqueId());
            if ((trueVictim instanceof Player)) {
                GameMode gameMode = ((Player) trueVictim).getGameMode();
                if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) return;
                for (EnumAbilities ab : mob.abilityList) {
                    ab.onAttackPlayer((LivingEntity) trueAttacker, mob, (Player) trueVictim, isDirectAttack, event);
                }
            }
            double extraDamage = event.getDamage() - originDamage;
            if (ConfigReader.isEnhanceEnabled()) {
                LevelConfig levelConfig = ConfigReader.getLevelConfig();
                double damage = levelConfig.getDamage(originDamage, mob.getMobLevel());
                damage += extraDamage;
                event.setDamage(damage);
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
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.mobManager.infernalNaturalSpawn(e);
            }
        }.runTaskLater(plugin, 10L);
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
        if (determineShouldDrop(killer != null, (killer != null) && (killer.getGameMode() == GameMode.CREATIVE || killer.getGameMode() == GameMode.SPECTATOR))) {
            ItemStack drop;
            if (!mob.isCustomMob) {
                drop = this.plugin.lootManager.getRandomLoot(killer, mob.getMobLevel());
            } else {
                drop = this.plugin.lootManager.getLootByName(killer, mob.customLoot);
            }
            if (drop != null && drop.getType() != Material.AIR) {
                final int percentage = ConfigReader.getDropChance();
                final int randomNum = new Random().nextInt(100);
                if (randomNum < percentage) {
                    event.getDrops().add(drop);
                    selectedDropItem = drop;
                }
            }
        }

        // set xp drop
        if (ConfigReader.isEnhanceEnabled()) {
            int xp = ConfigReader.getLevelConfig().getExp(event.getDroppedExp(), mob.getMobLevel());
            event.setDroppedExp(xp);
        } else {
            final int xpm = ConfigReader.getXpMultiplier();
            final int xp = event.getDroppedExp() * xpm;
            event.setDroppedExp(xp);
        }

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
            Message message = new Message("");

            if (this.plugin.getConfig().getList("deathMessages") != null) {
                String deathMessage = Helper.randomItem(plugin.getConfig().getStringList("deathMessages"));
                deathMessage = ChatColor.translateAlternateColorCodes('&', deathMessage);
                deathMessage = deathMessage.replace("{player}", playerName);
                deathMessage = deathMessage.replace("{mob}", mobName);
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null && !item.getType().equals(Material.AIR)) {
                    message.append(deathMessage, item);
                } else {
                    message.append(deathMessage.replace("{itemName}", "fist")
                            .replace("{itemName:0}", "fist"));
                }
            } else {
                System.out.println("No valid death messages found!");
            }
            message.append("\n");
            String msg = Helper.randomItem(plugin.getConfig().getStringList("nodropMessages"));
            if (plugin.getConfig().isList("dropMessages") && selectedDropItem != null) {
                msg = Helper.randomItem(plugin.getConfig().getStringList("dropMessages"));
//                message.append(msg);
//                if (broadcastToAllWorld) {
//                    new Message("")
//                            .append(msg, selectedDropItem)
//                            .broadcast();
//                } else {
//                    new Message("")
//                            .append(msg, selectedDropItem)
//                            .broadcast(player.getLocation().getWorld());
//                }
            }

            if (plugin.getConfig().isList("nodropMessages") && selectedDropItem == null) {
                msg = Helper.randomItem(plugin.getConfig().getStringList("nodropMessages"));
//                if (broadcastToAllWorld) {
//                    new Message(msg)
//                            .broadcast();
//                } else {
//                    new Message(msg)
//                            .broadcast(player.getLocation().getWorld());
//                }
            }
            msg = ChatColor.translateAlternateColorCodes('&', msg);
            msg = msg.replace("{player}", playerName);
            msg = msg.replace("{mob}", mobName);
            if (selectedDropItem == null){
                message.append(msg);
            }else {
                message.append(msg,selectedDropItem);
            }
            if (broadcastToAllWorld){
                Broadcaster.broadcastToAllWorld(message, player);
            }else {
                Broadcaster.broadcast(player.getLocation().getWorld(), message, player);
            }
        }

        plugin.mobManager.mobMap.remove(id);
        BossBarManager.removeMob(mob, mobEntity);
        // TODO event
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk() && event.getChunk() != null && ConfigReader.isEnabledWorld(event.getWorld())) {
            for (Entity entity : event.getChunk().getEntities()) {
                if (entity instanceof LivingEntity &&
                        plugin.mobManager.mobMap.get(entity.getUniqueId()) == null &&
                        entity.getCustomName() != null) {
                    if (ConfigReader.isInfernalMobNameTagAlwaysVisible() && !entity.isCustomNameVisible()) {
                        continue;
                    }
                    String prefix = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', ConfigReader.getNamePrefix()));
                    if (ChatColor.stripColor(entity.getCustomName()).startsWith(prefix)) {
                        entity.remove();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() != null && plugin.mobManager.mobMap.containsKey(event.getEntity().getUniqueId()) &&
                plugin.mobManager.mobMap.containsKey(event.getTarget().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionHit(PotionSplashEvent event) {
        ProjectileSource source = event.getEntity().getShooter();
        if (source instanceof LivingEntity &&
                plugin.mobManager.mobMap.containsKey(((LivingEntity) source).getUniqueId())) {
            List<LivingEntity> affectedEntities = new ArrayList<>();
            for (LivingEntity e : event.getAffectedEntities()) {
                if (plugin.mobManager.mobMap.containsKey(e.getUniqueId())) {
                    affectedEntities.add(e);
                }
            }
            for (LivingEntity e : affectedEntities) {
                event.setIntensity(e, 0.0D);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobTeleport(EntityTeleportEvent event) {
        if (plugin.mobManager.mobMap.containsKey(event.getEntity().getUniqueId()) && event.getFrom().getWorld() != event.getTo().getWorld()) {
            event.setCancelled(true);
        }
    }
}
