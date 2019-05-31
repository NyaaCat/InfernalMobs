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
import java.util.Objects;

public class BroadcastConfig extends FileConfigure {
    @Serializable
    int nearbyRange = 100;
    @Serializable
    Map<String, Integer> broadcastSettings = new LinkedHashMap<>();

    @Serializable
    int defaultType = ReceiveType.NEARBY.num;

    public void setReceivetype(String uuid, ReceiveType receiveType){
        broadcastSettings.put(uuid, receiveType.num);
        this.save();
    }

    public ReceiveType getReceiveType(String uuid){
        return ReceiveType.forNum(broadcastSettings.computeIfAbsent(uuid, s -> defaultType));
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

    public void globalSetting(ReceiveType receiveType) {
        Objects.requireNonNull(receiveType);
        defaultType = receiveType.num;
        broadcastSettings.entrySet().forEach(entry -> entry.setValue(receiveType.num));
        save();
    }

    public enum ReceiveType {
        ALL(0), NEARBY(1), SELF_ONLY(2), OFF(3);
        int num;
        ReceiveType(int in){
            num = in;
        }

        public static ReceiveType forNum(Integer num) {
            ReceiveType[] values = values();
            for (ReceiveType value : values) {
                if (value.num == num) {
                    return value;
                }
            }
            return ALL;
        }
    }
}
