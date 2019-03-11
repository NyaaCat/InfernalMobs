package com.jacob_vejvoda.infernal_mobs;

import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.loot.LootManager;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class InfernalMobs extends JavaPlugin {
    public static InfernalMobs instance;

    public List<Player> errorList; // players who used the "error" diagnose command
    public EventListener events;
    public CommandHandler cmd;
    public MobManager mobManager;
    public LootManager lootManager;

    public InfernalMobs() {
        this.errorList = new ArrayList<>();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        this.cmd = new CommandHandler(this);
        this.events = new EventListener(this);
        this.mobManager = new MobManager(this);
        this.lootManager = new LootManager(this);
        this.getCommand("infernalmobs").setExecutor(cmd);
        this.getServer().getPluginManager().registerEvents(this.events, this);

        // Start the main loop
        new BukkitRunnable(){
            @Override
            public void run() {
                mainLoop();
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    @Override
    public void onDisable() {
        for (World w : getServer().getWorlds()) {
            if (ConfigReader.isEnabledWorld(w)) {
                for (Entity e : w.getLivingEntities()) {
                    if (mobManager.mobMap.containsKey(e.getUniqueId())) {
                        mobManager.mobMap.remove(e.getUniqueId());
                        e.remove();
                    }
                }
            }
        }
    }

    /**
     * The main loop to display all effects
     * Called every second
     */
    public void mainLoop() {
        // update scoreboard
        for (Player p : Bukkit.getOnlinePlayers()) {
            GUI.refreshPlayerScoreboard(p);
            BossBarManager.updateBar();
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
