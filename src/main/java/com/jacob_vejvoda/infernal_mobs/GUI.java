package com.jacob_vejvoda.infernal_mobs;

import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BossBar support removed.
 * Scoreboard is used to display mob abilities
 */
public class GUI implements Listener {
    public static final int GUI_SCAN_DISTANCE = 26;
    private static final String OBJECTIVE_NAME_INFO = "im_info";
    private final static Map<UUID, Scoreboard> mobScoreboard = new HashMap<>();
    private final static BossBarManager barManager = new BossBarManager();

    private static InfernalMobs getPlugin() {
        return InfernalMobs.instance;
    }

    public static void refreshPlayerScoreboard(Player p) {
        // actually, update scoreboards for mobs around the player
        if (!ConfigReader.isScoreboardEnabled()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            return;
        }
        if (p.getScoreboard().getTeam("bloodmoon") != null && p.getScoreboard().getTeam("bloodmoon").hasEntry(p.getName())) {
            return;
        }
        // Select mob
        Mob mob = null;
        LivingEntity mobEntity = null;
        double minAngle = Math.PI / 2D;

        /*
          todo: select a specified amount of mobs near players.
            The amount is controlled by config file.
         */

        Set<Entity> nearbyInfMobs = p.getNearbyEntities(GUI_SCAN_DISTANCE, GUI_SCAN_DISTANCE, GUI_SCAN_DISTANCE).stream()
                .filter(entity -> entity instanceof LivingEntity && getPlugin().mobManager.mobMap.get(entity.getUniqueId()) != null)
                .collect(Collectors.toSet());

        BossBarManager.registerNearbyBossBar(p, nearbyInfMobs);

        for (Entity e : p.getNearbyEntities(GUI_SCAN_DISTANCE, GUI_SCAN_DISTANCE, GUI_SCAN_DISTANCE)) {
            if (!(e instanceof LivingEntity)) continue;
            Mob currentMob = getPlugin().mobManager.mobMap.get(e.getUniqueId());
            if (currentMob == null) continue;
            LivingEntity currentMobEntity = (LivingEntity) e;
            Vector eyeSight = p.getEyeLocation().getDirection();
            Vector mobVector = currentMobEntity.getEyeLocation().toVector().subtract(p.getEyeLocation().toVector());
            double angle = Helper.getVectorAngle(eyeSight, mobVector);
            if (!Double.isFinite(angle)) continue;
            if (angle >= minAngle) continue;
            minAngle = angle;
            mob = currentMob;
            mobEntity = currentMobEntity;
        }

        // update scoreboard for select mob
        if (mob != null) {
            updateMobScoreboard(mob, mobEntity);
            p.setScoreboard(mobScoreboard.get(mob.entityId));
        } else {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    private static void updateMobScoreboard(Mob mob, LivingEntity mobEntity) {
        Scoreboard sb = mobScoreboard.get(mob.entityId);
        if (sb == null) { // init scoreboard
            sb = Bukkit.getScoreboardManager().getNewScoreboard();
            mobScoreboard.put(mob.entityId, sb);
            Objective obj = sb.registerNewObjective(OBJECTIVE_NAME_INFO, "dummy");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.setDisplayName(mobEntity.getType().name());
            int index = 1; // this is the actual "score"
            for (EnumAbilities ab : mob.abilityList) {
                obj.getScore(ab.name().toLowerCase()).setScore(index++);
            }
            obj.getScore(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Abilities:").setScore(index++);
            double maxHealth = mobEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double health = mobEntity.getHealth();
            obj.getScore(String.format("Health: %.0f/%.0f", health, maxHealth)).setScore(index);
        } else { // update scoreboard
            Objective obj = sb.getObjective(OBJECTIVE_NAME_INFO);
            int index = -1;
            for (String str : sb.getEntries()) {
                if (str.startsWith("Health:")) {
                    index = obj.getScore(str).getScore();
                    sb.resetScores(str);
                }
            }
            double maxHealth = mobEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double health = mobEntity.getHealth();
            obj.getScore(String.format("Health: %.0f/%.0f", health, maxHealth)).setScore(index);
        }
    }
}
