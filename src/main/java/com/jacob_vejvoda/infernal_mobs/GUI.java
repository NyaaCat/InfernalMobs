package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class GUI implements Listener {
    static infernal_mobs plugin;
    static HashMap<String, Scoreboard> playerScoreBoard;
    static HashMap<Entity, BossBar> bossBars;

    static {
        GUI.playerScoreBoard = new HashMap<String, Scoreboard>();
        GUI.bossBars = new HashMap<Entity, BossBar>();
    }

    public GUI(final infernal_mobs instance) {
        GUI.plugin = instance;
    }

    public static void fixBar(final Player p) {
        double dis = 26.0;
        Entity b = null;
        for (final Mob m : plugin.mobManager.mobMap.values()) {
            if (m.entity.getWorld().equals(p.getWorld())) {
                final Entity boss = m.entity;
                if (p.getLocation().distance(boss.getLocation()) >= dis) {
                    continue;
                }
                dis = p.getLocation().distance(boss.getLocation());
                b = boss;
            }
        }
        if (b != null) {
            if (b.isDead() || ((Damageable) b).getHealth() <= 0.0) {
                plugin.removeMob(b.getUniqueId());
                clearInfo(p);
            } else {
                if (GUI.plugin.getConfig().getBoolean("enableBossBar")) {
                    showBossBar(p, b);
                }
                if (GUI.plugin.getConfig().getBoolean("enableScoreBoard")) {
                    fixScoreboard(p, b, GUI.plugin.mobManager.mobMap.get(b.getUniqueId()).abilityList);
                }
            }
        } else {
            clearInfo(p);
        }
    }

    public static void showBossBar(final Player p, final Entity e) {
        final ArrayList<String> oldMobAbilityList = GUI.plugin.mobManager.mobMap.get(e.getUniqueId()).abilityList;
        String tittle;
        if (GUI.plugin.getConfig().getString("bossBarsName") != null) {
            tittle = GUI.plugin.getConfig().getString("bossBarsName");
        } else {
            tittle = "&fLevel <powers> &fInfernal <mobName>";
        }
        String mobName = e.getType().getName();
        if (e.getType().equals((Object) EntityType.SKELETON)) {
            final Skeleton sk = (Skeleton) e;
            if (sk.getSkeletonType().equals((Object) Skeleton.SkeletonType.WITHER)) {
                mobName = "WitherSkeleton";
            }
        } else if (e.getType().equals((Object) EntityType.HORSE)) {
            mobName = "Horse";
        }
        String prefix = GUI.plugin.getConfig().getString("namePrefix");
        if (GUI.plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size()) != null) {
            prefix = GUI.plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size());
        }
        tittle = tittle.replace("<prefix>", prefix);
        tittle = tittle.replace("<mobName>", mobName);
        tittle = tittle.replace("<mobLevel>", new StringBuilder(String.valueOf(oldMobAbilityList.size())).toString());
        String abilities = GUI.plugin.generateString(5, oldMobAbilityList);
        int count = 4;
        try {
            do {
                abilities = GUI.plugin.generateString(count, oldMobAbilityList);
                if (--count <= 0) {
                    break;
                }
            } while (tittle.length() + abilities.length() + mobName.length() > 64);
        } catch (Exception x) {
            System.out.println("showBossBar error: ");
            x.printStackTrace();
        }
        tittle = tittle.replace("<abilities>", abilities);
        tittle = ChatColor.translateAlternateColorCodes('&', tittle);
        final float health = (float) ((Damageable) e).getHealth();
        final float maxHealth = (float) ((Damageable) e).getMaxHealth();
        final float setHealth = health * 100.0f / maxHealth;
    }

    public static void clearInfo(final Player player) {
        if (GUI.plugin.getConfig().getBoolean("enableScoreBoard")) {
            try {
                player.getScoreboard().resetScores((OfflinePlayer) player);
                player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
            } catch (Exception ex) {
            }
        }
    }

    public static void fixScoreboard(final Player player, final Entity e, final ArrayList<String> abilityList) {
        if (GUI.plugin.getConfig().getBoolean("enableScoreBoard") && e instanceof Damageable) {
            if (GUI.playerScoreBoard.get(player.getName()) == null) {
                final ScoreboardManager manager = Bukkit.getScoreboardManager();
                final Scoreboard board = manager.getNewScoreboard();
                GUI.playerScoreBoard.put(player.getName(), board);
            }
            final Scoreboard board = GUI.playerScoreBoard.get(player.getName());
            Objective o;
            if (board.getObjective(DisplaySlot.SIDEBAR) == null) {
                o = board.registerNewObjective(player.getName(), "dummy");
                o.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else {
                o = board.getObjective(DisplaySlot.SIDEBAR);
            }
            o.setDisplayName(e.getType().getName());
            for (final String s : board.getEntries()) {
                board.resetScores(s);
            }
            int score = 1;
            for (final String ability : abilityList) {
                o.getScore("§r" + ability).setScore(score);
                ++score;
            }
            o.getScore("§e§lAbilities:").setScore(score);
            if (GUI.plugin.getConfig().getBoolean("showHealthOnScoreBoard")) {
                ++score;
                final float health = (float) ((Damageable) e).getHealth();
                final float maxHealth = (float) ((Damageable) e).getMaxHealth();
                final double roundOff = Math.round(health * 100.0) / 100.0;
                o.getScore(String.valueOf(roundOff) + "/" + maxHealth).setScore(score);
                ++score;
                o.getScore("§e§lHealth:").setScore(score);
            }
            if (player.getScoreboard() == null || player.getScoreboard().getObjective(DisplaySlot.SIDEBAR) == null || player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName() == null || !player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals(board.getObjective(DisplaySlot.SIDEBAR).getName())) {
                player.setScoreboard(board);
            }
        }
    }

    public void setName(final Entity ent) {
        try {
            if (GUI.plugin.getConfig().getInt("nameTagsLevel") != 0) {
                final String tittle = this.getMobNameTag(ent);
                ((LivingEntity) ent).setCustomName(tittle);
                if (GUI.plugin.getConfig().getInt("nameTagsLevel") == 2) {
                    ((LivingEntity) ent).setCustomNameVisible(true);
                }
            }
        } catch (Exception x) {
            System.out.println("Error in setName: ");
            x.printStackTrace();
        }
    }

    public String getMobNameTag(final Entity entity) {
        final ArrayList<String> oldMobAbilityList = GUI.plugin.mobManager.mobMap.get(entity.getUniqueId()).abilityList;
        String tittle = null;
        try {
            if (GUI.plugin.getConfig().getString("nameTagsName") != null) {
                tittle = GUI.plugin.getConfig().getString("nameTagsName");
            } else {
                tittle = "&fInfernal <mobName>";
            }
            String mobName = entity.getType().getName();
            if (entity.getType().equals((Object) EntityType.SKELETON)) {
                final Skeleton sk = (Skeleton) entity;
                if (sk.getSkeletonType().equals((Object) Skeleton.SkeletonType.WITHER)) {
                    mobName = "WitherSkeleton";
                }
            } else if (entity.getType().equals((Object) EntityType.HORSE)) {
                mobName = "Horse";
            }
            tittle = tittle.replace("<mobName>", mobName);
            tittle = tittle.replace("<mobLevel>", new StringBuilder().append(oldMobAbilityList.size()).toString());
            String abilities = GUI.plugin.generateString(5, oldMobAbilityList);
            int count = 4;
            do {
                abilities = GUI.plugin.generateString(count, oldMobAbilityList);
                --count;
            } while (tittle.length() + abilities.length() + mobName.length() > 64);
            tittle = tittle.replace("<abilities>", abilities);
            String prefix = GUI.plugin.getConfig().getString("namePrefix");
            if (GUI.plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size()) != null) {
                prefix = GUI.plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size());
            }
            tittle = tittle.replace("<prefix>", prefix);
            tittle = ChatColor.translateAlternateColorCodes('&', tittle);
        } catch (Exception x) {
            GUI.plugin.getLogger().log(Level.SEVERE, x.getMessage());
            x.printStackTrace();
        }
        return tittle;
    }
}
