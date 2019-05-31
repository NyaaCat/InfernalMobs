package com.jacob_vejvoda.infernal_mobs.utils;

import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ArmorStandUtil {
    private static Map<Entity, ArmorStand> projectileSources = new LinkedHashMap<>();
    private static Map<Entity, Bat> victims = new LinkedHashMap<>();
    private static final String META_KEY = "im-armor-stand";

    public static Bat asVictim(Entity player) {
        Location clone = player.getLocation().clone();
//        clone.setY(255);
        return summonBAt(clone);
    }

    private static Bat summonAndRemoveLater(Entity entity, Location location, int delay) {
        Bat summon = summonBAt(location);
        removeLater(entity, summon, delay);
        return summon;
    }

    private static Bat summonBAt(Location location) {
        return location.getWorld().spawn(location, Bat.class, bat -> {
            bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 10, true, false, false));
            bat.setPersistent(false);
            bat.setCanPickupItems(false);
            bat.setGlowing(false);
            bat.setGravity(false);
            bat.setCollidable(false);
            bat.setSilent(true);
            bat.setMetadata(META_KEY, new FixedMetadataValue(InfernalMobs.instance, true));
        });
    }

    private static ArmorStand summon(Location location) {
        ArmorStand armorStand;
        armorStand = location.getWorld().spawn(location, ArmorStand.class, (e) -> {
            e.setVisible(false);
            e.setPersistent(false);
            e.setCanPickupItems(false);
            e.setGlowing(false);
            e.setBasePlate(false);
            e.setArms(false);
            e.setMarker(true);
//            e.setInvulnerable(true);
            e.setGravity(false);
            e.setCollidable(false);
            e.setMetadata(META_KEY, new FixedMetadataValue(InfernalMobs.instance, true));
        });
        return armorStand;
    }

    private static void removeLater(Entity entity, Bat armorStand, int delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                projectileSources.remove(entity);
                armorStand.remove();
            }
        }.runTaskLater(InfernalMobs.instance, delay);
    }
}
