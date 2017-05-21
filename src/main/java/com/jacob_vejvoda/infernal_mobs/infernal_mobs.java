package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class infernal_mobs extends JavaPlugin {
    static infernal_mobs instance;
    public GUI gui;
    public HashMap<Entity, Entity> mountList;
    public ArrayList<Player> errorList;
    public EventListener events;
    public CommandHandler cmd;
    public MobManager mobManager;
    public PersistStorage persist;
    public LootManager lootManager;

    public infernal_mobs() {
        this.mountList = new HashMap<>();
        this.errorList = new ArrayList<>();
    }

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        saveResource("loot.yml", false);
        persist = new PersistStorage(this, new File(getDataFolder(), "save.yml"));
        this.cmd = new CommandHandler(this);
        this.events = new com.jacob_vejvoda.infernal_mobs.EventListener(this);
        this.gui = new GUI(this);
        this.mobManager = new MobManager(this);
        this.lootManager = new LootManager(this);
        this.getCommand("infernalmobs").setExecutor(cmd);
        this.getServer().getPluginManager().registerEvents(this.events, this);
        this.getServer().getPluginManager().registerEvents(this.gui, this);
        this.getLogger().log(Level.INFO, "Registered Events.");
        persist.loadToMemory();
        this.showEffect();
    }

    @Override
    public void onDisable() {
        persist.saveToFile();
    }

    public void scoreCheck() {
        for (final Player p : this.getServer().getOnlinePlayers()) {
            GUI.fixBar(p);
        }
        final HashMap<Entity, Entity> tmp = (HashMap<Entity, Entity>) this.mountList.clone();
        for (final Map.Entry<Entity, Entity> hm : tmp.entrySet()) {
            if (hm.getKey() != null && !hm.getKey().isDead()) {
                if (!hm.getValue().isDead() || !(hm.getKey() instanceof LivingEntity)) {
                    continue;
                }
                final String fate = this.getConfig().getString("mountFate");
                if (fate.equals("death")) {
                    final LivingEntity le = (LivingEntity) hm.getKey();
                    le.damage(9.99999999E8);
                    this.mountList.remove(hm.getKey());
                } else {
                    if (!fate.equals("removal")) {
                        continue;
                    }
                    hm.getKey().remove();
                    this.getLogger().log(Level.INFO, "Entity remove due to Fate");
                    this.mountList.remove(hm.getKey());
                }
            } else {
                this.mountList.remove(hm.getKey());
            }
        }
    }

    public void addHealth(Mob m) {
        Entity ent = m.entity;
        List<String> powerList = m.abilityList;
        final double maxHealth = ((Damageable) ent).getHealth();
        float setHealth;
        if (this.getConfig().getBoolean("healthByPower")) {
            try {
                setHealth = (float) (maxHealth * m.abilityList.size());
            } catch (Exception e2) {
                setHealth = (float) (maxHealth * 5.0);
            }
        } else if (this.getConfig().getBoolean("healthByDistance")) {
            final Location l = ent.getWorld().getSpawnLocation();
            int i = (int) l.distance(ent.getLocation()) / this.getConfig().getInt("addDistance");
            if (i < 1) {
                i = 1;
            }
            final int add = this.getConfig().getInt("healthToAdd");
            setHealth = i * add;
        } else {
            final int healthMultiplier = this.getConfig().getInt("healthMultiplier");
            setHealth = (float) (maxHealth * healthMultiplier);
        }
        if (setHealth >= 1.0f) {
            try {
                ((LivingEntity) ent).setMaxHealth((double) setHealth);
                ((LivingEntity) ent).setHealth((double) setHealth);
            } catch (Exception e) {
                System.out.println("addHealth: " + e);
            }
        }
    }

    public void removeMob(UUID id) {
        mobManager.mobMap.remove(id);
    }

    public void ghostMove(final Entity g) {
        if (g.isDead()) {
            return;
        }
        final Vector v = g.getLocation().getDirection().multiply(0.3);
        g.setVelocity(v);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                try {
                    infernal_mobs.this.ghostMove(g);
                } catch (Exception ex) {
                }
            }
        }, 2L);
    }

    public boolean isBaby(final Entity mob) {
        if (mob.getType().equals((Object) EntityType.ZOMBIE)) {
            final Zombie zombie = (Zombie) mob;
            if (zombie.isBaby()) {
                return true;
            }
        } else if (mob.getType().equals((Object) EntityType.PIG_ZOMBIE)) {
            final PigZombie pigzombie = (PigZombie) mob;
            if (pigzombie.isBaby()) {
                return true;
            }
        }
        return false;
    }

    public String getEffect() {
        String effect = "mobSpawnerFire";
        try {
            final ArrayList<String> partTypes = (ArrayList<String>) this.getConfig().getStringList("mobParticles");
            effect = partTypes.get(new Random().nextInt(partTypes.size()));
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        return effect;
    }

    public void displayEffect(final Location l, String effect) {
        if (effect == null) {
            try {
                effect = this.getEffect();
            } catch (Exception e) {
                effect = "mobSpawnerFire";
            }
        }
        final String[] split = effect.split(":");
        effect = split[0];
        final int data1 = Integer.parseInt(split[1]);
        final int data2 = Integer.parseInt(split[2]);
        try {
            String f = "FLAME";
            if (effect.equals("potionBrake")) {
                f = "SWIRL";
            } else if (effect.equals("smoke")) {
                f = "SMOKE";
            } else if (effect.equals("blockBrake")) {
                f = "FOOTSTEP";
            } else if (effect.equals("hugeExplode")) {
                f = "BIG_EXPLODE";
            } else if (effect.equals("angryVillager")) {
                f = "THUNDERCLOUD";
            } else if (effect.equals("cloud")) {
                f = "CLOUD";
            } else if (effect.equals("criticalHit")) {
                f = "CRITICALS";
            } else if (effect.equals("mobSpell")) {
                f = "INVIS_SWIRL";
            } else if (effect.equals("enchantmentTable")) {
                f = "ENCHANTS";
            } else if (effect.equals("ender")) {
                f = "ENDER";
            } else if (effect.equals("explode")) {
                f = "EXPLODE";
            } else if (effect.equals("greenSparkle")) {
                f = "HAPPY";
            } else if (effect.equals("heart")) {
                f = "HEARTS";
            } else if (effect.equals("largeExplode")) {
                f = "LARGE_SMOKE";
            } else if (effect.equals("splash")) {
                f = "SPLASH";
            } else if (effect.equals("largeSmoke")) {
                f = "LARGE_SMOKE";
            } else if (effect.equals("lavaSpark")) {
                f = "LAVA_SPARK";
            } else if (effect.equals("magicCriticalHit")) {
                f = "ENCHANT_CRITS";
            } else if (effect.equals("noteBlock")) {
                f = "NOTES";
            } else if (effect.equals("tileDust")) {
                f = "ITEM_CRACK";
            } else if (effect.equals("colouredDust")) {
                f = "REDSTONE_DUST";
            } else if (effect.equals("flame")) {
                f = "FLAME";
            } else if (effect.equals("witchMagic")) {
                f = "WITCH_MAGIC";
            } else if (effect != null) {
                f = effect;
            }
            if (f != null) {
                this.displayParticle(f, l, 1.0, data1, data2);
            } else {
                l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, data2);
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * The main loop to display all effects
     * Called every second
     */
    public void showEffect() {
        try {
            this.scoreCheck();
            final Collection<Mob> mobs = mobManager.mobMap.values();
            for (final Mob m : mobs) {
                final Entity mob = getServer().getEntity(m.id);
                if (mob != null) {
                    m.entity = mob;
                } else {
                    continue;
                }
                if (mob.isValid() && !mob.isDead() && mob.getLocation() != null && mob.getLocation().getChunk().isLoaded()) {
                    final Location feet = mob.getLocation();
                    final Location head = mob.getLocation();
                    head.setY(head.getY() + 1.0);
                    if (this.getConfig().getBoolean("enableParticles")) {
                        this.displayEffect(feet, m.effect);
                        if (!this.isSmall(mob)) {
                            this.displayEffect(head, m.effect);
                        }
                        if (mob.getType().equals((Object) EntityType.ENDERMAN) || mob.getType().equals((Object) EntityType.IRON_GOLEM)) {
                            head.setY(head.getY() + 1.0);
                            this.displayEffect(head, m.effect);
                        }
                    }
                    final List<String> abilityList = m.abilityList;
                    if (mob.isDead()) {
                        continue;
                    }
                    for (final String ability : abilityList) {
                        final Random rand = new Random();
                        final int min = 1;
                        final int max = 10;
                        final int randomNum = rand.nextInt(max - min + 1) + min;
                        if (ability.equals("cloaked")) {
                            ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1), true);
                        } else if (ability.equals("armoured")) {
                            if (mob instanceof Skeleton || mob instanceof Zombie || mob instanceof PigZombie) {
                                continue;
                            }
                            ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1), true);
                        } else if (ability.equals("1up")) {
                            if (((Damageable) mob).getHealth() > 5.0) {
                                continue;
                            }
                            final Mob oneUpper = m;
                            if (oneUpper.lives <= 1) {
                                continue;
                            }
                            ((Damageable) mob).setHealth(((Damageable) mob).getMaxHealth());
                            oneUpper.setLives(oneUpper.lives - 1);
                        } else if (ability.equals("sprint")) {
                            ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1), true);
                        } else if (ability.equals("molten")) {
                            ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1), true);
                        } else if (ability.equals("tosser")) {
                            if (randomNum >= 6) {
                                continue;
                            }
                            final double radius = 6.0;
                            final ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                            for (final Player player : near) {
                                if (player.getLocation().distance(mob.getLocation()) <= radius && !player.isSneaking() && !player.getGameMode().equals((Object) GameMode.CREATIVE)) {
                                    player.setVelocity(mob.getLocation().toVector().subtract(player.getLocation().toVector()));
                                }
                            }
                        } else if (ability.equals("gravity")) {
                            if (randomNum < 9) {
                                continue;
                            }
                            final double radius = 10.0;
                            final ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                            for (final Player player : near) {
                                if (player.getLocation().distance(mob.getLocation()) <= radius) {
                                    final Location feetBlock = player.getLocation();
                                    feetBlock.setY(feetBlock.getY() - 2.0);
                                    final Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                                    if (block.getType().equals((Object) Material.AIR) || player.getGameMode().equals((Object) GameMode.CREATIVE)) {
                                        continue;
                                    }
                                    int amount = 6;
                                    if (this.getConfig().getString("gravityLevitateLength") != null) {
                                        amount = this.getConfig().getInt("gravityLevitateLength");
                                    }
                                    this.levitate((Entity) player, amount);
                                }
                            }
                        } else {
                            if ((!ability.equals("ghastly") && !ability.equals("necromancer")) || randomNum != 6 || mob.isDead()) {
                                continue;
                            }
                            final double radius = 20.0;
                            final ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                            for (final Player player : near) {
                                if (player.getLocation().distance(mob.getLocation()) <= radius && !player.getGameMode().equals((Object) GameMode.CREATIVE)) {
                                    Fireball fb = null;
                                    if (ability.equals("ghastly")) {
                                        fb = (Fireball) ((LivingEntity) mob).launchProjectile((Class) Fireball.class);
                                        player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 5.0f, 1.0f);

                                    } else {
                                        fb = (Fireball) ((LivingEntity) mob).launchProjectile((Class) WitherSkull.class);
                                    }
                                    this.moveToward((Entity) fb, player.getLocation(), 0.6);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                infernal_mobs.this.showEffect();
            }
        }, 20L);
    }

    public boolean isSmall(final Entity mob) {
        return this.isBaby(mob) && mob.getType().equals((Object) EntityType.BAT) && mob.getType().equals((Object) EntityType.CAVE_SPIDER) && mob.getType().equals((Object) EntityType.CHICKEN) && mob.getType().equals((Object) EntityType.COW) && mob.getType().equals((Object) EntityType.MUSHROOM_COW) && mob.getType().equals((Object) EntityType.PIG) && mob.getType().equals((Object) EntityType.OCELOT) && mob.getType().equals((Object) EntityType.SHEEP) && mob.getType().equals((Object) EntityType.SILVERFISH) && mob.getType().equals((Object) EntityType.SPIDER) && mob.getType().equals((Object) EntityType.WOLF);
    }

    public void moveToward(final Entity e, final Location to, final double speed) {
        if (e.isDead()) {
            return;
        }
        final Vector direction = to.toVector().subtract(e.getLocation().toVector()).normalize();
        e.setVelocity(direction.multiply(speed));
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                try {
                    infernal_mobs.this.moveToward(e, to, speed);
                } catch (Exception ex) {
                }
            }
        }, 1L);
    }

    private boolean isArmor(final ItemStack s) {
        final String t = s.getType().toString().toLowerCase();
        return t.contains("helm") || t.contains("plate") || t.contains("leg") || t.contains("boot");
    }

    private void showEffectParticles(final Entity p, final String e, final int time) {
        this.displayEffect(p.getLocation(), e);
        final int nt = time - 1;
        if (time > 0) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
                @Override
                public void run() {
                    infernal_mobs.this.showEffectParticles(p, e, nt);
                }
            }, 20L);
        }
    }

    private void levitate(final Entity e, final int time) {
        boolean couldFly = false;
        if (e instanceof Player && ((Player) e).getAllowFlight()) {
            couldFly = true;
        }
        final boolean couldFly2 = couldFly;
        if (e instanceof Player) {
            ((Player) e).setAllowFlight(true);
        }
        for (int i = 0; i < 40; ++i) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
                @Override
                public void run() {
                    final Vector vec = e.getVelocity();
                    vec.add(new Vector(0.0, 0.1, 0.0));
                    e.setVelocity(vec);
                }
            }, (long) i);
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                infernal_mobs.this.airHold(e, time - 2, couldFly2);
            }
        }, 20L);
    }

    public void airHold(final Entity e, final int time, final boolean couldFly) {
        for (int i = 0; i < time * 20; ++i, ++i) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
                @Override
                public void run() {
                    final Vector vec = e.getVelocity();
                    vec.setY(0.01);
                    e.setVelocity(vec);
                }
            }, (long) i);
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                if (e instanceof Player) {
                    ((Player) e).setAllowFlight(false);
                }
            }
        }, (long) (20 * time));
    }

    public boolean doEffect(final Player player, final Entity mob, final boolean playerIsVictom) throws Exception {
        if (!playerIsVictom) {
            final ItemStack itemUsed = player.getItemInHand();
            final ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            for (int i = 0; i < 9; ++i) {
                final ItemStack in = player.getInventory().getItem(i);
                if (in != null) {
                    items.add(in);
                }
            }
            ItemStack[] armorContents;
            for (int length = (armorContents = player.getInventory().getArmorContents()).length, j = 0; j < length; ++j) {
                final ItemStack ar = armorContents[j];
                if (ar != null) {
                    items.add(ar);
                }
            }
        }
        try {
            final UUID id = mob.getUniqueId();
            Mob m = mobManager.mobMap.get(id);
            if (m == null) return false;
            final List<String> abilityList = m.abilityList;
            if (!player.isDead() && !mob.isDead()) {
                for (final String ability : abilityList) {
                    this.doMagic(player, m, playerIsVictom, ability, id);
                }
                return true;
            }
            return false;
        } catch (Exception ex3) {
            return false;
        }
    }

    public void doMagic(final Entity vic, final Mob m, final boolean playerIsVictom, final String ability, final UUID id) {
        final int min = 1;
        final int max = 10;
        int randomNum = new Random().nextInt(max - min + 1) + min;
        Entity atc = m.entity;
        if (atc instanceof Player) {
            randomNum = 1;
        }
        try {
            if (atc instanceof Player) {
                if (ability.equals("tosser")) {
                    if (!(vic instanceof Player) || (!((Player) vic).isSneaking() && !((Player) vic).getGameMode().equals((Object) GameMode.CREATIVE))) {
                        vic.setVelocity(atc.getLocation().toVector().subtract(vic.getLocation().toVector()));
                    }
                } else if (ability.equals("gravity")) {
                    if (!(vic instanceof Player) || (!((Player) vic).isSneaking() && !((Player) vic).getGameMode().equals((Object) GameMode.CREATIVE))) {
                        final Location feetBlock = vic.getLocation();
                        feetBlock.setY(feetBlock.getY() - 2.0);
                        final Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                        if (!block.getType().equals((Object) Material.AIR)) {
                            int amount = 6;
                            if (this.getConfig().getString("gravityLevitateLength") != null) {
                                amount = this.getConfig().getInt("gravityLevitateLength");
                            }
                            this.levitate(vic, amount);
                        }
                    }
                } else if ((ability.equals("ghastly") || ability.equals("necromancer")) && !vic.isDead() && (!(vic instanceof Player) || (!((Player) vic).isSneaking() && !((Player) vic).getGameMode().equals((Object) GameMode.CREATIVE)))) {
                    Fireball fb = null;
                    if (ability.equals("ghastly")) {
                        fb = (Fireball) ((LivingEntity) atc).launchProjectile((Class) Fireball.class);
                    } else {
                        fb = (Fireball) ((LivingEntity) atc).launchProjectile((Class) WitherSkull.class);
                    }
                    this.moveToward((Entity) fb, vic.getLocation(), 0.6);
                }
            }
            if (ability.equals("ender")) {
                atc.teleport(vic.getLocation());
            } else if (ability.equals("poisonous") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1), true);
            } else if (ability.equals("morph") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                try {
                    final int mc = new Random().nextInt(25) + 1;
                    if (mc!=20) return;
                    final Location l = atc.getLocation().clone();
                    final double h = ((Damageable) atc).getHealth();
                    final List<String> aList = m.abilityList;
                    final double dis = 46.0;
                    for (final Entity e : atc.getNearbyEntities(dis, dis, dis)) {
                        if (e instanceof Player) {
                            GUI.fixBar((Player) e);
                        }
                    }
                    atc.teleport(new Location(atc.getWorld(), l.getX(), 0.0, l.getZ()));
                    atc.remove();
                    this.getLogger().log(Level.INFO, "Entity remove due to Morph");
                    final ArrayList<String> mList = (ArrayList<String>) this.getConfig().getList("enabledmobs");
                    final int index = new Random().nextInt(mList.size());
                    final String mobName = mList.get(index);
                    Entity newEnt = null;
                    EntityType entityType = EntityType.valueOf(mobName);
                    newEnt = vic.getWorld().spawnEntity(l, entityType);
                    if (newEnt == null) {
                        System.out.println("Infernal Mobs can't find mob type: " + mobName + "!");
                        return;
                    }
                    mobManager.mobMap.remove(m.id);
                    m.entity = newEnt;
                    m.id = newEnt.getUniqueId();
                    mobManager.mobMap.put(m.id, m);
                    //Mob newMob = null;
                    if (aList.contains("1up")) {
                        m.lives = 2;
                    } else {
                        m.lives = 1;
                    }
                    if (aList.contains("flying")) {
                        this.mobManager.makeFly(newEnt);
                    }
                    this.gui.setName(newEnt);
                    this.mobManager.giveMobGear(newEnt, true);
                    this.addHealth(m);
                    if (h >= ((Damageable) newEnt).getMaxHealth()) {
                        return;
                    }
                    ((Damageable) newEnt).setHealth(h);
                } catch (Exception ex) {
                    System.out.print("Morph Error: ");
                    ex.printStackTrace();
                }
            }
            if (ability.equals("molten") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                int amount2;
                if (this.getConfig().getString("moltenBurnLength") != null) {
                    amount2 = this.getConfig().getInt("moltenBurnLength");
                } else {
                    amount2 = 5;
                }
                vic.setFireTicks(amount2 * 20);
            } else if (ability.equals("blinding") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1), true);
            } else if (ability.equals("confusing") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 2), true);
            } else if (ability.equals("withering") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 180, 1), true);
            } else if (ability.equals("thief") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                if (vic instanceof Player) {
                    if (((Player) vic).getInventory().getItemInHand() != null && !((Player) vic).getInventory().getItemInHand().getType().equals((Object) Material.AIR) && (randomNum <= 1 || randomNum == 1)) {
                        vic.getWorld().dropItemNaturally(atc.getLocation(), ((Player) vic).getInventory().getItemInHand());
                        final int slot = ((Player) vic).getInventory().getHeldItemSlot();
                        ((Player) vic).getInventory().setItem(slot, (ItemStack) null);
                    }
                } else if (vic instanceof PigZombie || vic instanceof Zombie || vic instanceof Skeleton) {
                    final EntityEquipment eq = ((LivingEntity) vic).getEquipment();
                    if (eq.getItemInHand() != null) {
                        vic.getWorld().dropItemNaturally(atc.getLocation(), eq.getItemInHand());
                        eq.setItemInHand((ItemStack) null);
                    }
                }
            } else if (ability.equals("quicksand") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 180, 1), true);
            } else if (ability.equals("bullwark") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 500, 2), true);
            } else if (ability.equals("rust") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                if (((Player) vic).getInventory().getItemInHand() != null) {
                    final ItemStack damItem = ((Player) vic).getInventory().getItemInHand();
                    if ((randomNum <= 3 || randomNum == 1) && damItem.getMaxStackSize() == 1) {
                        final int cDur = damItem.getDurability();
                        damItem.setDurability((short) (cDur + 20));
                    }
                }
            } else if (ability.equals("sapper") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 500, 1), true);
            } else if (!ability.equals("1up") || !this.isLegitVictim(atc, playerIsVictom, ability)) {
                if (ability.equals("ender") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                    final Location targetLocation = vic.getLocation();
                    if (randomNum >= 8) {
                        final Random rand2 = new Random();
                        final int min2 = 1;
                        final int max2 = 4;
                        final int randomNum2 = rand2.nextInt(max2 - min2 + 1) + min2;
                        if (randomNum2 == 1) {
                            targetLocation.setZ(targetLocation.getZ() + 6.0);
                        } else if (randomNum == 2) {
                            targetLocation.setZ(targetLocation.getZ() - 5.0);
                        } else if (randomNum == 3) {
                            targetLocation.setX(targetLocation.getX() + 8.0);
                        } else if (randomNum == 4) {
                            targetLocation.setX(targetLocation.getX() - 10.0);
                        }
                        final Location needAir1 = targetLocation;
                        final Location needAir2 = targetLocation;
                        final Location needAir3 = targetLocation;
                        needAir2.setY(needAir2.getY() + 1.0);
                        needAir3.setY(needAir3.getY() + 2.0);
                        if ((needAir1.getBlock().getType().equals((Object) Material.AIR) || needAir1.getBlock().getType().equals((Object) Material.TORCH)) && (needAir2.getBlock().getType().equals((Object) Material.AIR) || needAir2.getBlock().getType().equals((Object) Material.TORCH)) && (needAir3.getBlock().getType().equals((Object) Material.AIR) || needAir3.getBlock().getType().equals((Object) Material.TORCH))) {
                            atc.teleport(targetLocation);
                        }
                    }
                } else if (ability.equals("lifesteal") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                    ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 1), true);
                } else if (!ability.equals("cloaked") || !this.isLegitVictim(atc, playerIsVictom, ability)) {
                    if (ability.equals("storm") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                        if ((randomNum <= 2 || randomNum == 1) && !atc.isDead()) {
                            vic.getWorld().strikeLightning(vic.getLocation());
                        }
                    } else if (!ability.equals("sprint") || !this.isLegitVictim(atc, playerIsVictom, ability)) {
                        if (ability.equals("webber") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum >= 8 || randomNum == 1) {
                                final Location feet = vic.getLocation();
                                feet.getBlock().setType(Material.WEB);
                                this.setAir(feet, 60);
                                final int rNum = new Random().nextInt(max - min + 1) + min;
                                if (rNum == 5 && (atc.getType().equals((Object) EntityType.SPIDER) || atc.getType().equals((Object) EntityType.CAVE_SPIDER))) {
                                    final Location k = atc.getLocation();
                                    final Block b = k.getBlock();
                                    final List<Block> blocks = Helper.getSphere(b, 4);
                                    for (final Block bl : blocks) {
                                        if (bl.getType().equals((Object) Material.AIR)) {
                                            bl.setType(Material.WEB);
                                            this.setAir(bl.getLocation(), 30);
                                        }
                                    }
                                }
                            }
                        } else if (ability.equals("vengeance") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum >= 5 || randomNum == 1) {
                                int amount3;
                                if (this.getConfig().getString("vengeanceDamage") != null) {
                                    amount3 = this.getConfig().getInt("vengeanceDamage");
                                } else {
                                    amount3 = 6;
                                }
                                if (vic instanceof LivingEntity) {
                                    ((LivingEntity) vic).damage((double) (int) Math.round(2.0 * amount3));
                                }
                            }
                        } else if (ability.equals("weakness") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 1), true);
                        } else if (ability.equals("berserk") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum >= 5 && !atc.isDead()) {
                                final double health = ((Damageable) atc).getHealth();
                                ((Damageable) atc).setHealth(health - 1.0);
                                int amount4;
                                if (this.getConfig().getString("berserkDamage") != null) {
                                    amount4 = this.getConfig().getInt("berserkDamage");
                                } else {
                                    amount4 = 3;
                                }
                                if (vic instanceof LivingEntity) {
                                    ((LivingEntity) vic).damage((double) (int) Math.round(2.0 * amount4));
                                }
                            }
                        } else if (ability.equals("potions") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            Potion potion = null;
                            if (randomNum == 5) {
                                potion = new Potion(PotionType.INSTANT_DAMAGE, 2);
                            } else if (randomNum == 6) {
                                potion = new Potion(PotionType.INSTANT_DAMAGE, 1);
                            } else if (randomNum == 7) {
                                potion = new Potion(PotionType.WEAKNESS, 2);
                            } else if (randomNum == 8) {
                                potion = new Potion(PotionType.POISON, 2);
                            } else if (randomNum == 9) {
                                potion = new Potion(PotionType.SLOWNESS, 2);
                            }
                            if (potion != null) {
                                potion.setSplash(true);
                                final ItemStack iStack = new ItemStack(Material.POTION);
                                potion.apply(iStack);
                                final Location sploc = atc.getLocation();
                                sploc.setY(sploc.getY() + 3.0);
                                final ThrownPotion thrownPotion = (ThrownPotion) vic.getWorld().spawnEntity(sploc, EntityType.SPLASH_POTION);
                                thrownPotion.setItem(iStack);
                                final Vector direction = atc.getLocation().getDirection();
                                direction.normalize();
                                direction.add(new Vector(0.0, 0.2, 0.0));
                                double dist = atc.getLocation().distance(vic.getLocation());
                                dist /= 15.0;
                                direction.multiply(dist);
                                thrownPotion.setVelocity(direction);
                            }
                        } else if (ability.equals("mama") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum == 1) {
                                int amount3;
                                if (this.getConfig().getString("mamaSpawnAmount") != null) {
                                    amount3 = this.getConfig().getInt("mamaSpawnAmount");
                                } else {
                                    amount3 = 3;
                                }
                                if (atc.getType().equals((Object) EntityType.MUSHROOM_COW)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final MushroomCow minion = (MushroomCow) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.MUSHROOM_COW);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.COW)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final Cow minion2 = (Cow) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.COW);
                                        minion2.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.SHEEP)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final Sheep minion3 = (Sheep) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.SHEEP);
                                        minion3.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.PIG)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final Pig minion4 = (Pig) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.PIG);
                                        minion4.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.CHICKEN)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final Chicken minion5 = (Chicken) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.CHICKEN);
                                        minion5.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.WOLF)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final Wolf minion6 = (Wolf) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.WOLF);
                                        minion6.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.ZOMBIE)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final Zombie minion7 = (Zombie) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.ZOMBIE);
                                        minion7.setBaby(true);
                                    }
                                } else if (atc.getType().equals((Object) EntityType.PIG_ZOMBIE)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final PigZombie minion8 = (PigZombie) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.PIG_ZOMBIE);
                                        minion8.setBaby(true);
                                    }
                                } else if (atc.getType().equals((Object) EntityType.OCELOT)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final Ocelot minion9 = (Ocelot) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.OCELOT);
                                        minion9.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.HORSE)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final Horse minion10 = (Horse) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.HORSE);
                                        minion10.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.VILLAGER)) {
                                    for (int i = 0; i < amount3; ++i) {
                                        final Villager minion11 = (Villager) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.VILLAGER);
                                        minion11.setBaby();
                                    }
                                } else {
                                    for (int i = 0; i < amount3; ++i) {
                                        atc.getWorld().spawnEntity(atc.getLocation(), atc.getType());
                                    }
                                }
                            }
                        } else if (ability.equals("archer") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum > 7 || randomNum == 1) {
                                final ArrayList<Arrow> arrowList = new ArrayList<Arrow>();
                                final Location loc1 = vic.getLocation();
                                final Location loc2 = atc.getLocation();
                                if (!this.isSmall(atc)) {
                                    loc2.setY(loc2.getY() + 1.0);
                                }
                                final Arrow a = ((LivingEntity) atc).launchProjectile(Arrow.class);
                                final int arrowSpeed = 1;
                                loc2.setY((double) (loc2.getBlockY() + 2));
                                loc2.setX(loc2.getBlockX() + 0.5);
                                loc2.setZ(loc2.getBlockZ() + 0.5);
                                final Arrow a2 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), (float) arrowSpeed, 12.0f);
                                a2.setShooter((ProjectileSource) atc);
                                loc2.setY((double) (loc2.getBlockY() + 2));
                                loc2.setX((double) (loc2.getBlockX() - 1));
                                loc2.setZ((double) (loc2.getBlockZ() - 1));
                                final Arrow a3 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), (float) arrowSpeed, 12.0f);
                                a3.setShooter((ProjectileSource) atc);
                                arrowList.add(a);
                                arrowList.add(a2);
                                arrowList.add(a3);
                                for (final Arrow ar : arrowList) {
                                    double minAngle = 6.283185307179586;
                                    Entity minEntity = null;
                                    for (final Entity entity : atc.getNearbyEntities(64.0, 64.0, 64.0)) {
                                        if (((LivingEntity) atc).hasLineOfSight(entity) && entity instanceof LivingEntity && !entity.isDead()) {
                                            final Vector toTarget = entity.getLocation().toVector().clone().subtract(atc.getLocation().toVector());
                                            final double angle = ar.getVelocity().angle(toTarget);
                                            if (angle >= minAngle) {
                                                continue;
                                            }
                                            minAngle = angle;
                                            minEntity = entity;
                                        }
                                    }
                                    if (minEntity != null) {
                                        new ArrowHomingTask(ar, (LivingEntity) minEntity, (Plugin) this);
                                    }
                                }
                            }
                        } else if (ability.equals("firework") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            final int red = this.getConfig().getInt("fireworkColour.red");
                            final int green = this.getConfig().getInt("fireworkColour.green");
                            final int blue = this.getConfig().getInt("fireworkColour.blue");
                            final ItemStack tmpCol = new ItemStack(Material.LEATHER_HELMET, 1);
                            final LeatherArmorMeta tmpCol2 = (LeatherArmorMeta) tmpCol.getItemMeta();
                            tmpCol2.setColor(Color.fromRGB(red, green, blue));
                            final Color col = tmpCol2.getColor();
                            this.launchFirework(atc.getLocation(), col, 1);
                        }
                    }
                }
            }
        } catch (Exception ex3) {
        }
    }

    public void launchFirework(final Location l, final Color c, final int speed) {
        final Firework fw = (Firework) l.getWorld().spawn(l, (Class) Firework.class);
        final FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(c).with(FireworkEffect.Type.BALL_LARGE).build());
        fw.setFireworkMeta(meta);
        fw.setVelocity(l.getDirection().multiply(speed));
        this.detonate(fw);
    }

    public void detonate(final Firework fw) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                try {
                    fw.detonate();
                } catch (Exception ex) {
                }
            }
        }, 2L);
    }

    public boolean isLegitVictim(final Entity e, final boolean playerIsVictom, final String ability) {
        if (e instanceof Player) {
            return true;
        }
        if (this.getConfig().getBoolean("effectAllPlayerAttacks")) {
            return true;
        }
        final ArrayList<String> attackAbilityList = new ArrayList<String>();
        attackAbilityList.add("poisonous");
        attackAbilityList.add("blinding");
        attackAbilityList.add("withering");
        attackAbilityList.add("thief");
        attackAbilityList.add("sapper");
        attackAbilityList.add("lifesteal");
        attackAbilityList.add("storm");
        attackAbilityList.add("webber");
        attackAbilityList.add("weakness");
        attackAbilityList.add("berserk");
        attackAbilityList.add("potions");
        attackAbilityList.add("archer");
        attackAbilityList.add("confusing");
        if (playerIsVictom && attackAbilityList.contains(ability)) {
            return true;
        }
        final ArrayList<String> defendAbilityList = new ArrayList<String>();
        defendAbilityList.add("thief");
        defendAbilityList.add("storm");
        defendAbilityList.add("webber");
        defendAbilityList.add("weakness");
        defendAbilityList.add("potions");
        defendAbilityList.add("archer");
        defendAbilityList.add("quicksand");
        defendAbilityList.add("bullwark");
        defendAbilityList.add("rust");
        defendAbilityList.add("ender");
        defendAbilityList.add("vengeance");
        defendAbilityList.add("mama");
        defendAbilityList.add("firework");
        defendAbilityList.add("morph");
        return !playerIsVictom && defendAbilityList.contains(ability);
    }

    public void setAir(final Location block, final int time) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                if (block.getBlock().getType().equals((Object) Material.WEB)) {
                    block.getBlock().setType(Material.AIR);
                }
            }
        }, (long) (time * 20));
    }

    public ArrayList<String> getAbilitiesAmount(final Entity e) {
        return getAbilitiesAmount(e.getLocation());
    }

    public ArrayList<String> getAbilitiesAmount(final Location spawnLoc) {
        int power;
        if (this.getConfig().getBoolean("powerByDistance")) {
            final Location l = spawnLoc.getWorld().getSpawnLocation();
            int m = (int) l.distance(spawnLoc) / this.getConfig().getInt("addDistance");
            if (m < 1) {
                m = 1;
            }
            final int add = this.getConfig().getInt("powerToAdd");
            power = m * add;
        } else {
            final int min = this.getConfig().getInt("minpowers");
            final int max = this.getConfig().getInt("maxpowers");
            power = Helper.rand(min, max);
        }
        return this.getAbilities(power);
    }

    public ArrayList<String> getAbilities(final int amount) {
        final ArrayList<String> abilityList = new ArrayList<String>();
        final ArrayList<String> allAbilitiesList = new ArrayList<String>(Arrays.<String>asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
        final int min = 1;
        for (int i = 0; i < amount; ++i) {
            final int max = allAbilitiesList.size();
            final int randomNum = new Random().nextInt(max - min + 1) + min - 1;
            final String ab = allAbilitiesList.get(randomNum);
            if (this.getConfig().getString(ab) != null) {
                if (this.getConfig().getString(ab).equals("always") || this.getConfig().getBoolean(ab)) {
                    abilityList.add(ab);
                    allAbilitiesList.remove(randomNum);
                } else {
                    allAbilitiesList.remove(randomNum);
                    --i;
                }
            } else {
                this.getLogger().log(Level.WARNING, "Ability: " + ab + " is not set!");
            }
        }
        return abilityList;
    }

    public Entity getTarget(final Player player) {
        final BlockIterator iterator = new BlockIterator(player.getWorld(), player.getLocation().toVector(), player.getEyeLocation().getDirection(), 0.0, 100);
        Entity target = null;
        while (iterator.hasNext()) {
            final Block item = iterator.next();
            for (final Entity entity : player.getNearbyEntities(100.0, 100.0, 100.0)) {
                for (int acc = 2, x = -acc; x < acc; ++x) {
                    for (int z = -acc; z < acc; ++z) {
                        for (int y = -acc; y < acc; ++y) {
                            if (entity.getLocation().getBlock().getRelative(x, y, z).equals(item)) {
                                return target = entity;
                            }
                        }
                    }
                }
            }
        }
        return target;
    }

    public void displayParticle(final String effect, final Location l, final double radius, final int speed, final int amount) {
        this.displayParticle(effect, l.getWorld(), l.getX(), l.getY(), l.getZ(), radius, speed, amount);
    }

    private void displayParticle(final String effect, final World w, final double x, final double y, final double z, final double radius, final int speed, final int amount) {
        final Location l = new Location(w, x, y, z);
        try {
            if (radius == 0.0) {
                ParticleEffects.sendToLocation(ParticleEffects.valueOf(effect), l, 0.0f, 0.0f, 0.0f, speed, amount);
            } else {
                final ArrayList<Location> ll = this.getArea(l, radius, 0.2);
                for (int i = 0; i < amount; ++i) {
                    final int index = new Random().nextInt(ll.size());
                    ParticleEffects.sendToLocation(ParticleEffects.valueOf(effect), ll.get(index), 0.0f, 0.0f, 0.0f, speed, 1);
                    ll.remove(index);
                }
            }

        } catch (Exception ex) {
            System.out.println("V: " + this.getServer().getVersion());
            ex.printStackTrace();
        }
    }

    private ArrayList<Location> getArea(final Location l, final double r, final double t) {
        final ArrayList<Location> ll = new ArrayList<Location>();
        for (double x = l.getX() - r; x < l.getX() + r; x += t) {
            for (double y = l.getY() - r; y < l.getY() + r; y += t) {
                for (double z = l.getZ() - r; z < l.getZ() + r; z += t) {
                    ll.add(new Location(l.getWorld(), x, y, z));
                }
            }
        }
        return ll;
    }

    public String getRandomMob() {
        final ArrayList<String> mobList = (ArrayList<String>) this.getConfig().getList("enabledmobs");
        if (mobList.isEmpty()) {
            return "Zombie";
        }
        final String mob = mobList.get(Helper.rand(1, mobList.size()) - 1);
        if (mob != null) {
            return mob;
        }
        return "Zombie";
    }

    public String generateString(int maxNames, final ArrayList<String> names) {
        String namesString = "";
        if (maxNames > names.size()) {
            maxNames = names.size();
        }
        for (int i = 0; i < maxNames; ++i) {
            namesString = String.valueOf(namesString) + names.get(i) + " ";
        }
        if (names.size() > maxNames) {
            namesString = String.valueOf(namesString) + "... ";
        }
        return namesString;
    }

    public void reloadLoot() {
        this.lootManager.reload();
    }

}
