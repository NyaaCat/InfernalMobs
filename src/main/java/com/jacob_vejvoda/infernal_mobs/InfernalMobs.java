package com.jacob_vejvoda.infernal_mobs;

import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import com.jacob_vejvoda.infernal_mobs.persist.PersistStorage;
import com.jacob_vejvoda.infernal_mobs.loot.LootManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class InfernalMobs extends JavaPlugin {
    public static InfernalMobs instance;

    public List<Player> errorList; // players who used the "error" diagnose command
    public EventListener events;
    public CommandHandler cmd;
    public MobManager mobManager;
    public PersistStorage persist;
    public LootManager lootManager;

    public InfernalMobs() {
        this.errorList = new ArrayList<>();
    }

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        saveResource("loot.yml", false);
        persist = new PersistStorage(this, new File(getDataFolder(), "save.yml"));
        this.cmd = new CommandHandler(this);
        this.events = new EventListener(this);
        this.mobManager = new MobManager(this);
        this.lootManager = new LootManager(this);
        this.getCommand("infernalmobs").setExecutor(cmd);
        this.getServer().getPluginManager().registerEvents(this.events, this);
        persist.loadToMemory();

        // Start the main loop
        new BukkitRunnable(){
            @Override
            public void run() {
                mainLoop();
            }
        }.runTaskTimer(this, 20L, 20L);
        new BukkitRunnable(){
            @Override
            public void run() {
                persist.saveToFile();
            }
        }.runTaskTimer(this, 600*20, 600*20);
    }

    @Override
    public void onDisable() {
        persist.saveToFile();
    }

    /**
     * The main loop to display all effects
     * Called every second
     */
    public void mainLoop() {
        // update scoreboard
        for (Player p : Bukkit.getOnlinePlayers()) {
            GUI.refreshPlayerScoreboard(p);
        }

        // clear mountee mobs
        for (Iterator<UUID> iter = mobManager.mounteeMobs.iterator(); iter.hasNext(); ) {
            UUID entityId = iter.next();
            Entity e = Bukkit.getEntity(entityId);
            if (e == null) {
                iter.remove();
                continue;
            }
            if (e.getPassengers().size() <= 0) {
                if (ConfigReader.isKillMountee() && e instanceof LivingEntity) {
                    ((LivingEntity) e).damage(9.99999999E8);
                    iter.remove();
                } else if (ConfigReader.isRemovalMountee()) {
                    e.remove();
                    iter.remove();
                }
            }
        }

        for (Mob mob : mobManager.mobMap.values()) {
            Entity e = Bukkit.getEntity(mob.entityId);
            if (!(e instanceof LivingEntity)) continue;
            if (!e.isValid() || e.isDead() || e.getLocation() == null || !e.getLocation().getChunk().isLoaded()) continue;
            LivingEntity mobEntity = (LivingEntity) e;

            // send particle effects
            Location feet = mobEntity.getLocation();
            Location eye = mobEntity.getEyeLocation();
            if (ConfigReader.particlesEnabled()) {
                mob.particleEffect.spawnAt(feet);
                if (feet.distanceSquared(eye) > 1)
                    mob.particleEffect.spawnAt(eye);
            }

            // Per-cycle abilities
            for (EnumAbilities ab : mob.abilityList) {
                ab.perCycleEffect(mobEntity, mob);
            }
        }
    }

    public void reloadLoot() {
        this.lootManager.reload();
    }
}
