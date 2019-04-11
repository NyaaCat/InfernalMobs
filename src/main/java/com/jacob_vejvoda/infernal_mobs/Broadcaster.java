package com.jacob_vejvoda.infernal_mobs;

import cat.nyaa.nyaacore.Message;
import com.jacob_vejvoda.infernal_mobs.config.BroadcastConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Broadcaster {
    public static void broadcast(World world, Message message, Entity source){
        Bukkit.getScheduler().runTaskAsynchronously(InfernalMobs.instance, ()->{
            message.broadcast(Message.MessageType.CHAT, player -> player.getWorld().equals(world) && shouldReceiveMessage(player, source));
        });
    }

    private static boolean shouldReceiveMessage(Player player, Entity source) {
        BroadcastConfig broadcastConfig = ConfigReader.getBroadcastConfig();
        BroadcastConfig.ReceiveType receiveType = broadcastConfig.getReceiveType(player.getUniqueId().toString());
        switch (receiveType){
            case ALL:
                return true;
            case SELF_ONLY:
                return player.getUniqueId().equals(source.getUniqueId());
            case OFF:
                return false;
        }
        int nearbyRange = broadcastConfig.getNearbyRange();
        return player.equals(source) || player.getNearbyEntities(nearbyRange,nearbyRange,nearbyRange).contains(source);
    }

    public static void broadcastToAllWorld(Message message, Entity source){
        Bukkit.getScheduler().runTaskAsynchronously(InfernalMobs.instance, ()->{
            message.broadcast(Message.MessageType.CHAT, player -> shouldReceiveMessage(player, source));
        });
    }
}
