package com.jacob_vejvoda.infernal_mobs.config;

import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import com.jacob_vejvoda.infernal_mobs.I18n;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class BroadcastConfig extends FileConfigure {
    @Serializable
    int nearbyRange = 100;
    @Serializable
    Map<String, ReceiveType> broadcastSettings = new LinkedHashMap<>();

    public void setReceivetype(String uuid, ReceiveType receiveType){
        broadcastSettings.put(uuid, receiveType);
        this.save();
    }

    public ReceiveType getReceiveType(String uuid){
        return broadcastSettings.computeIfAbsent(uuid, s -> ReceiveType.ALL);
    }

    @Override
    protected String getFileName() {
        return "broadcast.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return InfernalMobs.instance;
    }

    public int getNearbyRange() {
        return nearbyRange;
    }

    public void toggle(Player sender) {
        ReceiveType receiveType = getReceiveType(sender.getUniqueId().toString());
        switch (receiveType){
            case ALL:
                receiveType = ReceiveType.NEARBY;
                break;
            case NEARBY:
                receiveType = ReceiveType.SELF_ONLY;
                break;
            case SELF_ONLY:
                receiveType = ReceiveType.OFF;
                break;
            case OFF:
                receiveType = ReceiveType.ALL;
                break;
        }
        setReceivetype(sender.getUniqueId().toString(), receiveType);
        sendHint(sender,receiveType);
    }

    public void sendHint(Player sender, ReceiveType type) {
        Message message = new Message("");
        switch (type){
            case ALL:
                message.append(I18n.format("imi.state_all"));
                break;
            case NEARBY:
                message.append(I18n.format("imi.state_near"));
                break;
            case SELF_ONLY:
                message.append(I18n.format("imi.state_self"));
                break;
            case OFF:
                message.append(I18n.format("imi.state_off"));
                break;
        }
        message.send(sender);
    }

    public enum ReceiveType implements ISerializable{
        ALL, NEARBY, SELF_ONLY, OFF
    }
}
