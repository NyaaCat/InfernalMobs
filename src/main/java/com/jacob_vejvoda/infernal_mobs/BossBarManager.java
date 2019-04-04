package com.jacob_vejvoda.infernal_mobs;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BossBarManager {
    private final static Map<BossBar, HashSet<Player>> barPlayerMap = new HashMap<>();
    private final static Map<Player, HashSet<BossBar>> playerBarMap = new HashMap<>();
    private final static BiMap<LivingEntity, BossBar> bossBarMap = HashBiMap.create();

    private static final Object registerLock = new Object();

    static void registerNearbyBossBar(Player p, Set<Entity> nearbyInfMobs) {
        int maxBars = getMaxBarCount();
        HashSet<BossBar> bossBars = playerBarMap.computeIfAbsent(p, k -> new HashSet<>());
//        bossBars.clear();
        List<AngledEntity> angleSet = new ArrayList<>();
        nearbyInfMobs.forEach(entity -> {
            LivingEntity currentMobEntity = (LivingEntity) entity;
            Vector eyeSight = p.getEyeLocation().getDirection();
            Vector mobVector = currentMobEntity.getEyeLocation().toVector().subtract(p.getEyeLocation().toVector());
            double angle = Helper.getVectorAngle(eyeSight, mobVector);
            if (!Double.isFinite(angle)) {
                return;
            }
            angleSet.add(new AngledEntity(angle, calcDistance(p, entity), currentMobEntity));
        });
        List<AngledEntity> collect = angleSet.stream().sorted().limit(maxBars).collect(Collectors.toList());

        synchronized (registerLock) {
            bossBars.clear();
            if (!collect.isEmpty()) {
                collect.forEach(angle -> {
                    BossBar bossBar = getBossBar(angle.livingEntity);
                    registerPlayer(bossBar, p);
                });
            }
        }
    }

    private static double calcDistance(Player p, Entity entity) {
        return p.getLocation().distanceSquared(entity.getLocation());
    }

    private static void registerPlayer(BossBar bossBar, Player p) {
        synchronized (playerBarMap) {
            synchronized (barPlayerMap) {
                HashSet<Player> barPlayers = barPlayerMap.computeIfAbsent(bossBar, k -> new HashSet<>());
                HashSet<BossBar> playerBars = playerBarMap.computeIfAbsent(p, k -> new HashSet<>());
                playerBars.add(bossBar);
                barPlayers.add(p);
            }
        }
    }

    static void updateBar() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(InfernalMobs.instance, () -> {
            if (!barPlayerMap.isEmpty()) {
                synchronized (registerLock) {
                    List<BossBar> removeList = new ArrayList<>();
                    synchronized (barPlayerMap) {
                        barPlayerMap.forEach((bossBar, players) -> {
                            if (bossBar == null) return;
                            if (!players.isEmpty()) {
                                for (Player player : players) {
                                    HashSet<BossBar> bossBars = playerBarMap.computeIfAbsent(player, player1 -> new HashSet<>());
                                    if (bossBars.contains(bossBar)) {
                                        bossBar.addPlayer(player);
                                    } else {
                                        bossBar.removePlayer(player);
                                    }
                                }
                            }
                            BiMap<BossBar, LivingEntity> inverse = bossBarMap.inverse();
                            LivingEntity livingEntity = inverse.get(bossBar);
                            if (livingEntity == null) {
                                bossBar.removeAll();
                                inverse.remove(bossBar);
                                removeList.add(bossBar);
                                return;
                            }
//                            refreshBar(bossBar, livingEntity);
                        });
                        removeList.forEach(bossBar -> {
                            barPlayerMap.remove(bossBar);
                            Bukkit.getScheduler().runTask(InfernalMobs.instance, () ->
                                    playerBarMap.forEach((player, bossBars) -> bossBars.remove(bossBar)));

                        });
                    }
                }
            }
        }, 1);
    }

    private static void refreshBar(BossBar bossBar, LivingEntity livingEntity) {
        double health = livingEntity.getHealth();
        double maxHealth = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double progress = health / maxHealth;
        bossBar.setProgress(progress);
        if (progress < 0.33) {
            bossBar.setColor(BarColor.RED);
        } else if (progress < 0.66) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.BLUE);
        }
    }

    private static BossBar getBossBar(LivingEntity livingEntity) {
        BossBar bossBar = bossBarMap.computeIfAbsent(livingEntity, k -> {
            String customName = livingEntity.getCustomName() == null ? livingEntity.getName() : livingEntity.getCustomName();
            BossBar bossBar1 = Bukkit.createBossBar(customName, BarColor.BLUE, BarStyle.SEGMENTED_10);
            refreshBar(bossBar1, livingEntity);
            return bossBar1;
        });
        return bossBar;
    }

    private static int getMaxBarCount() {
        //todo: read max bar count from config
        return 5;
    }

    public static void removeMob(Mob mob, LivingEntity mobEntity) {
        Bukkit.getScheduler().runTaskAsynchronously(InfernalMobs.instance, () -> {
            BossBar bossBar = bossBarMap.get(mobEntity);
            if (bossBar == null) return;
            HashSet<Player> players = barPlayerMap.computeIfAbsent(bossBar, (bar) -> new HashSet<>());
            if (!players.isEmpty()) {
                players.forEach(player -> {
                    HashSet<BossBar> bossBars = playerBarMap.get(player);
                    bossBars.remove(bossBar);
                });
            }
            players.clear();
            Bukkit.getScheduler().runTaskLater(InfernalMobs.instance, bossBar::removeAll, 20);
            bossBar.setTitle(bossBar.getTitle().concat(ConfigReader.getBossbarDeathHint()));
            refreshBar(bossBar, mobEntity);
            synchronized (bossBarMap) {
                synchronized (barPlayerMap) {
                    bossBarMap.remove(mobEntity);
                    barPlayerMap.remove(bossBar);
                }
            }
        });
    }

    public static void updateMob(Mob im) {
        Entity entity = Bukkit.getServer().getEntity(im.entityId);
        if (entity instanceof LivingEntity) {
            BossBar bossBar = bossBarMap.get(entity);
            if (bossBar != null) {
                Bukkit.getScheduler().runTask(InfernalMobs.instance, ()-> refreshBar(bossBar, (LivingEntity) entity));
            }
        }
    }

    static class AngledEntity implements Comparable<AngledEntity> {
        private static int CLOSE_DISTANCE = (GUI.GUI_SCAN_DISTANCE * GUI.GUI_SCAN_DISTANCE) / 4;
        double angle;
        double distance;
        LivingEntity livingEntity;

        public AngledEntity(double angle, double distance, LivingEntity currentMobEntity) {
            this.angle = angle;
            this.distance = distance;
            livingEntity = currentMobEntity;
        }

        @Override
        public int hashCode() {
            return (int) (angle + livingEntity.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AngledEntity && angle == ((AngledEntity) obj).angle && livingEntity.equals(((AngledEntity) obj).livingEntity);
        }

        @Override
        public int compareTo(AngledEntity o) {
            double c1, c2;
            c1 = angle;
            c2 = o.angle;
            double distanceShift = 1000000d;
            if (distance > CLOSE_DISTANCE) {
                c1 += distanceShift;
            }
            if (o.distance > CLOSE_DISTANCE) {
                c2 += distanceShift;
            }
            if (c1 - c2 > 0) return 1;
            if (c1 == c2) return 0;
            return -1;
        }
    }
}
