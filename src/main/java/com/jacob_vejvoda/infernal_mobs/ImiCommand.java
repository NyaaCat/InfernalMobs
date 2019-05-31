package com.jacob_vejvoda.infernal_mobs;

import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import com.jacob_vejvoda.infernal_mobs.config.BroadcastConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ImiCommand extends CommandReceiver {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (args.length == 0) {
            if (sender.hasPermission("infernal_mobs.imi")) {
                toggleState(((Player) sender));
                return true;
            }
        }
        return super.onCommand(sender, command, label, args);
    }

    @SubCommand("all")
    public void onAll(CommandSender sender, Arguments arguments) {
        if (!sender.hasPermission("infernal_mobs.imi")) return;
        if (!(sender instanceof Player)) return;
        setState(((Player) sender), BroadcastConfig.ReceiveType.ALL);
    }

    @SubCommand("me")
    public void onSelfOnly(CommandSender sender, Arguments arguments) {
        if (!sender.hasPermission("infernal_mobs.imi")) return;
        if (!(sender instanceof Player)) return;
        setState(((Player) sender), BroadcastConfig.ReceiveType.SELF_ONLY);
    }

    @SubCommand("near")
    public void onNearby(CommandSender sender, Arguments arguments) {
        if (!sender.hasPermission("infernal_mobs.imi")) return;
        if (!(sender instanceof Player)) return;
        setState(((Player) sender), BroadcastConfig.ReceiveType.NEARBY);
    }

    @SubCommand("off")
    public void onOff(CommandSender sender, Arguments arguments) {
        if (!sender.hasPermission("infernal_mobs.imi")) return;
        if (!(sender instanceof Player)) return;
        setState(((Player) sender), BroadcastConfig.ReceiveType.OFF);
    }

    @SubCommand("global")
    public void onGlobalChange(CommandSender sender, Arguments arguments) {
        if (!sender.isOp()) {
            new Message(I18n.format("error.permission"))
                    .send(sender);
            return;
        }
        String arg1 = arguments.nextString();
        try {
            BroadcastConfig.ReceiveType receiveType = BroadcastConfig.ReceiveType.valueOf(arg1);
            Bukkit.getScheduler().runTaskAsynchronously(InfernalMobs.instance, () -> {
                BroadcastConfig broadcastConfig = ConfigReader.getBroadcastConfig();
                broadcastConfig.globalSetting(receiveType);
                new Message(I18n.format("imi.global_success",receiveType.name()))
                .send(sender);
            });
        } catch (Exception e) {
            new Message(I18n.format("error.wrong_argument"));
        }
    }

    private void setState(Player sender, BroadcastConfig.ReceiveType type) {
        BroadcastConfig broadcastConfig = ConfigReader.getBroadcastConfig();
        broadcastConfig.setReceivetype(sender.getUniqueId().toString(), type);
        broadcastConfig.sendHint(sender, type);
    }

    private void toggleState(Player sender) {
        ConfigReader.getBroadcastConfig().toggle(sender);
    }

    public ImiCommand(JavaPlugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }
}
