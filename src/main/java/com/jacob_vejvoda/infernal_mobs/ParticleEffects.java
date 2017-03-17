package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public enum ParticleEffects {
    BIG_EXPLODE("BIG_EXPLODE", 0, "EXPLOSION_HUGE", 0),
    BLOCK_CRACK("BLOCK_CRACK", 1, "BLOCK_CRACK", 0),
    BLOCK_DUST("BLOCK_DUST", 2, "BLOCK_DUST", 0),
    BUBBLES("BUBBLES", 3, "WATER_BUBBLE", 0),
    CLOUD("CLOUD", 4, "CLOUD", 0),
    CRITICALS("CRITICALS", 5, "CRIT", 0),
    ENCHANT_CRITS("ENCHANT_CRITS", 6, "CRIT_MAGIC", 0),
    ENCHANTS("ENCHANTS", 7, "ENCHANTMENT_TABLE", 0),
    ENDER("ENDER", 8, "PORTAL", 0),
    EXPLODE("EXPLODE", 9, "EXPLOSION_NORMAL", 0),
    FLAME("FLAME", 10, "FLAME", 0),
    FIREWORKS("FIREWORKS", 11, "FIREWORKS_SPARK", 0),
    FOOTSTEP("FOOTSTEP", 12, "FOOTSTEP", 0),
    HAPPY("HAPPY", 13, "VILLAGER_HAPPY", 0),
    HEARTS("HEARTS", 14, "HEART", 0),
    ITEM_CRACK("ITEM_CRACK", 15, "ITEM_CRACK", 0),
    ITEM_TAKE("ITEM_TAKE", 16, "ITEM_TAKE", 0),
    INVIS_SWIRL("INVIS_SWIRL", 17, "SPELL_MOB_AMBIENT", 0),
    LARGE_EXPLODE("LARGE_EXPLODE", 18, "EXPLOSION_LARGE", 0),
    LARGE_SMOKE("LARGE_SMOKE", 19, "SMOKE_LARGE", 0),
    LAVA_SPARK("LAVA_SPARK", 20, "LAVA", 0),
    LAVA("LAVA", 21, "DRIP_LAVA", 0),
    MOB_APPEARANCE("MOB_APPEARANCE", 22, "MOB_APPEARANCE", 0),
    NOTES("NOTES", 23, "NOTE", 5),
    REDSTONE_DUST("REDSTONE_DUST", 24, "REDSTONE", 0),
    SLIME("SLIME", 25, "SLIME", 0),
    SMOKE("SMOKE", 26, "SMOKE_NORMAL", 0),
    SNOW("SNOW", 27, "SNOW_SHOVEL", 0),
    SNOWBALL("SNOWBALL", 28, "SNOWBALL", 0),
    SPLASH("SPLASH", 29, "SPELL_INSTANT", 0),
    SUSPEND("SUSPEND", 30, "SUSPENDED", 0),
    SWIRL("SWIRL", 31, "SPELL_MOB", 0),
    WHITE_SWIRL("WHITE_SWIRL", 32, "SPELL", 1),
    THUNDERCLOUD("THUNDERCLOUD", 33, "VILLAGER_ANGRY", 0),
    TOWN_AURA("TOWN_AURA", 34, "TOWN_AURA", 0),
    VOID("VOID", 35, "SUSPENDED_DEPTH", 0),
    WATER_SPLASH("WATER_SPLASH", 36, "WATER_SPLASH", 0),
    WATER("WATER", 37, "DRIP_WATER", 0),
    WATER_DROP("WATER_DROP", 38, "WATER_DROP", 0),
    WATER_WAKE("WATER_WAKE", 39, "WATER_WAKE", 0),
    WITCH_MAGIC("WITCH_MAGIC", 40, "SPELL_WITCH", 0);

    private String name;
    private int id;
    private Particle nativeParticle;

    private ParticleEffects(final String s, final int n, final String name, final int id) {
        this.name = name;
        this.id = id;
        nativeParticle = Particle.valueOf(name);
    }

    public static void sendToPlayer(final ParticleEffects effect, final Player player, final Location location, final float offsetX, final float offsetY, final float offsetZ, final float speed, final int count) {
        try {
            player.spawnParticle(effect.nativeParticle, location, count, offsetX, offsetY, offsetZ, speed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendToLocation(final ParticleEffects effect, final Location location, final float offsetX, final float offsetY, final float offsetZ, final float speed, final int count) {
        try {
            location.getWorld().spawnParticle(effect.nativeParticle, location, count, offsetX, offsetY, offsetZ, speed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    String getName() {
        return this.name;
    }

    int getId() {
        return this.id;
    }
}
