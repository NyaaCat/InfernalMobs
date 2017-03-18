package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Helper {
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

    public static int rand(final int min, final int max) {
        final int r = min + (int) (Math.random() * (1 + max - min));
        return r;
    }

    /**
     * write stream to file in data folder
     */
    public static void writeToDataFile(JavaPlugin plugin, InputStream inputStream, String fileName) {
        try {
            Files.copy(inputStream, new File(plugin.getDataFolder(), fileName).toPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
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

    public static String getLocationName(final Location l) {
        return (String.valueOf(l.getX()) + "." + l.getY() + "." + l.getZ() + l.getWorld().getName()).replace(".", "");
    }

    public static <T> T randomItem(List<T> list) {
        final Random randomGenerator = new Random();
        final int index = randomGenerator.nextInt(list.size());
        return list.get(index);
    }

    public static void changeLeatherColor(final ItemStack item, final Color color) {
        try {
            final LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(color);
            item.setItemMeta((ItemMeta) meta);
        } catch (Exception ex) {
        }
    }
}
