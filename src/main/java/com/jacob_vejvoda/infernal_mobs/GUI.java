package com.jacob_vejvoda.infernal_mobs;

import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BossBar support removed.
 * Scoreboard is used to display mob abilities
 */
public class GUI implements Listener {
    public static final int GUI_SCAN_DISTANCE = 26;
    private static final String OBJECTIVE_NAME_INFO = "infernal_mob_info";
    private final static Map<UUID, Scoreboard> mobScoreboard = new HashMap<>();

    private static InfernalMobs getPlugin() {
        return InfernalMobs.instance;
    }

    public static void refreshPlayerScoreboard(Player p) {
        if (!ConfigReader.isScoreboardEnabled()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            return;
        }

        // Select mob
        Mob mob = null;
        LivingEntity mobEntity = null;
        double minAngle = Math.PI/2D;
        for (Entity e : p.getNearbyEntities(GUI_SCAN_DISTANCE, GUI_SCAN_DISTANCE, GUI_SCAN_DISTANCE)) {
            if (!(e instanceof LivingEntity)) continue;
            Mob currentMob = getPlugin().mobManager.mobMap.get(e.getUniqueId());
            if (currentMob == null) continue;
            LivingEntity currentMobEntity = (LivingEntity)e;
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
                obj.getScore(ab.name().toLowerCase()).setScore(index);
            }
            // TODO Health and level info
        } else { // update scoreboard
            // TODO Health and level info
        }
    }

//    public static void fixScoreboard(final Player player, final Entity e, final ArrayList<String> abilityList) {
//        if (GUI.plugin.getConfig().getBoolean("enableScoreBoard") && e instanceof Damageable) {
//            if (GUI.playerScoreBoard.get(player.getName()) == null) {
//                final ScoreboardManager manager = Bukkit.getScoreboardManager();
//                final Scoreboard board = manager.getNewScoreboard();
//                GUI.playerScoreBoard.put(player.getName(), board);
//            }
//            final Scoreboard board = GUI.playerScoreBoard.get(player.getName());
//            Objective o;
//            if (board.getObjective(DisplaySlot.SIDEBAR) == null) {
//                o = board.registerNewObjective(player.getName(), "dummy");
//                o.setDisplaySlot(DisplaySlot.SIDEBAR);
//            } else {
//                o = board.getObjective(DisplaySlot.SIDEBAR);
//            }
//            o.setDisplayName(e.getType().getName());
//            for (final String s : board.getEntries()) {
//                board.resetScores(s);
//            }
//            int score = 1;
//            for (final String ability : abilityList) {
//                o.getScore(ChatColor.RESET.toString() + ability).setScore(score);
//                ++score;
//            }
//            o.getScore(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Abilities:").setScore(score);
//            if (GUI.plugin.getConfig().getBoolean("showHealthOnScoreBoard")) {
//                ++score;
//                final float health = (float) ((Damageable) e).getHealth();
//                final float maxHealth = (float) ((Damageable) e).getMaxHealth();
//                final double roundOff = Math.round(health * 100.0) / 100.0;
//                o.getScore(String.valueOf(roundOff) + "/" + maxHealth).setScore(score);
//                ++score;
//                o.getScore(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Health:").setScore(score);
//            }
//            if (player.getScoreboard() == null || player.getScoreboard().getObjective(DisplaySlot.SIDEBAR) == null || player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName() == null || !player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals(board.getObjective(DisplaySlot.SIDEBAR).getName())) {
//                player.setScoreboard(board);
//            }
//        }
//    }
//
//    public static String generateString(int maxNames, final ArrayList<String> names) {
//        String namesString = "";
//        if (maxNames > names.size()) {
//            maxNames = names.size();
//        }
//        for (int i = 0; i < maxNames; ++i) {
//            namesString = String.valueOf(namesString) + names.get(i) + " ";
//        }
//        if (names.size() > maxNames) {
//            namesString = String.valueOf(namesString) + "... ";
//        }
//        return namesString;
//    }
}
