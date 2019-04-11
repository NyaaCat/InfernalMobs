package com.jacob_vejvoda.infernal_mobs;

import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import com.jacob_vejvoda.infernal_mobs.config.CustomMobConfig;
import com.jacob_vejvoda.infernal_mobs.config.LevelConfig;
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
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InfernalMobs extends JavaPlugin {
    public static InfernalMobs instance;

    public List<Player> errorList; // players who used the "error" diagnose command
    public EventListener events;
    public CommandHandler cmd;
    public MobManager mobManager;
    public LootManager lootManager;
    public LevelConfig levelConfig;
    public CustomMobConfig customMobConfig;
    private AsyncInfernalTicker asyncInfernalTicker;
    private BukkitTask bukkitTask;
    private BukkitRunnable mainLoopRunnable;

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
        I18n i18n = new I18n();
        i18n.load();
        ImiCommand imiCommand = new ImiCommand(this, i18n);
        this.getCommand("infernalinfo").setExecutor(imiCommand);

        // Start the main loop

        mainLoopRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                mainLoop();
            }
        };
        reloadMainLoopTask();
        asyncInfernalTicker = new AsyncInfernalTicker();
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

    public void reloadMainLoopTask() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
        int mobRandomTick = ConfigReader.getMobRandomTick();
        if (mobRandomTick < 10){
            getLogger().log(Level.WARNING, "invalid random tick "+ mobRandomTick+", min is 10, set to 20.");
            mobRandomTick = 20;
        }
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                mainLoop();
            }
        }.runTaskTimer(this, 0, mobRandomTick);
    }

    /**
     * The main loop to display all effects
     * Called every second
     */
    public void mainLoop() {
        // update scoreboard
        for (Player p : Bukkit.getOnlinePlayers()) {
            GUI.refreshPlayerScoreboard(p);
            GUI.refreshBossBar(p);
        }
        BossBarManager.updateBar();

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

        int maxQueueSize = asyncInfernalTicker.getMaxQueueSize();
        List<Mob> collect = mobManager.mobMap.values().stream()
                .limit(maxQueueSize).collect(Collectors.toList());
        asyncInfernalTicker.submitInfernalTickMobs(collect);

//
//        for (Mob mob : mobManager.mobMap.values()) {
//            Entity e = Bukkit.getEntity(mob.entityId);
//            if (!(e instanceof LivingEntity)) continue;
//            if (!e.isValid() || e.isDead() || e.getLocation() == null || !e.getLocation().getChunk().isLoaded()) continue;
//            LivingEntity mobEntity = (LivingEntity) e;
//
//            // send particle effects
//            Location feet = mobEntity.getLocation();
//            Location eye = mobEntity.getEyeLocation();
//            if (ConfigReader.particlesEnabled()) {
//                mob.particleEffect.spawnAt(feet);
//                if (feet.distanceSquared(eye) > 1)
//                    mob.particleEffect.spawnAt(eye);
//            }
//
//            // Per-cycle abilities
//            for (EnumAbilities ab : mob.abilityList) {
//                ab.perCycleEffect(mobEntity, mob);
//            }
//        }
    }

    public void reloadLoot() {
        this.lootManager.reload();
    }

    private class AsyncInfernalTicker {
        private final BukkitScheduler scheduler;
        Queue<Mob> mobEffectQueue;
        private int lastTickMobCount = 0;
        private int nextTickTasks = 0;
        private boolean previousTaskFinished = true;
        private boolean overload = false;
        private int maxQueueSize = Integer.MAX_VALUE;

        AsyncInfernalTicker() {
            scheduler = Bukkit.getScheduler();
            mobEffectQueue = new LinkedList<>();
            Bukkit.getScheduler().runTaskTimer(InfernalMobs.this, () -> {
                tick();
                end();
            }, 0, 1);
        }

        void tick() {
            if (mobEffectQueue.isEmpty()) return;
//            if (!previousTaskFinished && !overload){
//                getLogger().log(Level.WARNING, "previous server tick didn't finished, maybe there's too much task to do.");
//                overload = true;
//            }else if (overload){
//
//            }
            previousTaskFinished = false;
            for (int i = 0; i < nextTickTasks; i++) {
                if (mobEffectQueue.isEmpty()) return;
                Mob mob = mobEffectQueue.poll();
                Entity e = Bukkit.getEntity(mob.entityId);
                if (!(e instanceof LivingEntity)) continue;
                if (!e.isValid() || e.isDead() || e.getLocation() == null || !e.getLocation().getChunk().isLoaded())
                    continue;
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

        private void end() {
            previousTaskFinished = true;
        }

        public void submitInfernalTickMobs(List<Mob> mobs) {
            if (mobs == null || mobs.isEmpty()) return;
            mobs.forEach(mob -> mobEffectQueue.offer(mob));
            double mobRandomTick = ((double) ConfigReader.getMobRandomTick());
            nextTickTasks = (int) Math.ceil((mobs.size()) / mobRandomTick);
        }

        public int getLastTickMobCount() {
            return lastTickMobCount;
        }

        public int getMaxQueueSize() {
            return maxQueueSize;
        }
    }
}
