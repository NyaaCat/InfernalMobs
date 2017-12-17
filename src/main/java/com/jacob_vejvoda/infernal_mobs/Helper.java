package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.*;

public class Helper {
    public static final Random rnd = new Random(System.currentTimeMillis());

    public static List<Block> getSphere(final Block block1, final int radius) {
        final List<Block> blocks = new LinkedList<Block>();
        final double xi = block1.getLocation().getX() + 0.5;
        final double yi = block1.getLocation().getY() + 0.5;
        final double zi = block1.getLocation().getZ() + 0.5;
        for (int v1 = 0; v1 <= 90; ++v1) {
            final double y = Math.sin(0.017453292519943295 * v1) * radius;
            double r = Math.cos(0.017453292519943295 * v1) * radius;
            if (v1 == 90) {
                r = 0.0;
            }
            for (int v2 = 0; v2 <= 90; ++v2) {
                final double x = Math.sin(0.017453292519943295 * v2) * r;
                double z = Math.cos(0.017453292519943295 * v2) * r;
                if (v2 == 90) {
                    z = 0.0;
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)));
                }
            }
        }
        return blocks;
    }

    // generate random int between min and max (both inclusive)
    public static int rand(final int min, final int max) {
        return rnd.nextInt(max - min + 1) + min;
    }

    public static double rand(double min, double max) {
        return rnd.nextDouble() * (max-min) + min;
    }

    public static Block blockNear(final Location l, final Material mat, final int radius) {
        final double xTmp = l.getX();
        final double yTmp = l.getY();
        final double zTmp = l.getZ();
        final int finalX = (int) Math.round(xTmp);
        final int finalY = (int) Math.round(yTmp);
        final int finalZ = (int) Math.round(zTmp);
        for (int x = finalX - radius; x <= finalX + radius; ++x) {
            for (int y = finalY - radius; y <= finalY + radius; ++y) {
                for (int z = finalZ - radius; z <= finalZ + radius; ++z) {
                    final Location loc = new Location(l.getWorld(), (double) x, (double) y, (double) z);
                    final Block block = loc.getBlock();
                    if (block.getType().equals((Object) mat)) {
                        return block;
                    }
                }
            }
        }
        return null;
    }

    public static <T> T randomItem(List<T> list) {
        if (list == null || list.size() <= 0) return null;
        final Random randomGenerator = new Random();
        final int index = randomGenerator.nextInt(list.size());
        return list.get(index);
    }

    public static <T> List<T> randomNItems(List<T> list, int n) {
        if (list == null) return null;
        if (list.size() <= n) return new ArrayList<>(list);
        List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        return new ArrayList<T>(copy.subList(0, n));
    }

    public static void changeLeatherColor(final ItemStack item, final Color color) {
        try {
            final LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(color);
            item.setItemMeta(meta);
        } catch (Exception ex) {
        }
    }

    // x in [0,1], return true with possibility of x
    public static boolean possibility(double x) {
        if (x <= 0) return false;
        if (x >= 1) return true;
        return rnd.nextDouble() < x;
    }

    public static double getVectorAngle(Vector v1, Vector v2) {
        double dot = v1.dot(v2);
        double normalProduct = v1.length() * v2.length();
        double cos = dot/normalProduct;
        return Math.acos(cos);
    }

    public static void removeEntityLater(Entity e, int ticks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                e.remove();
            }
        }.runTaskLater(InfernalMobs.instance, ticks);
    }

    public static Vector unitDirectionVector(Vector from, Vector to) {
        Vector vec = to.clone().subtract(from);
        if (!Double.isFinite(vec.getX())) vec.setX(0D);
        if (!Double.isFinite(vec.getY())) vec.setY(0D);
        if (!Double.isFinite(vec.getZ())) vec.setZ(0D);
        if (vec.lengthSquared() == 0) return new Vector(0,0,0);
        return vec.normalize();
    }
}
