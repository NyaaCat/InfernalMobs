package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;


public class MobManager {
    public final Map<UUID, Mob> mobMap = new HashMap<>();
    private final infernal_mobs plugin;

    public MobManager(infernal_mobs plugin) {
        this.plugin = plugin;
    }

    public Mob spawnMob(EntityType type, Location loc, ArrayList<String> abilities) {
        Entity spawnedEntity = loc.getWorld().spawnEntity(loc, type);
        UUID id = spawnedEntity.getUniqueId();
        int living = abilities.contains("1up") ? 2 : 1;
        Mob mob = new Mob(spawnedEntity, id, loc.getWorld(), true, abilities, living, plugin.getEffect());
        if (abilities.contains("flying")) {
            plugin.mobManager.makeFly(spawnedEntity);
        }
        mobMap.put(id, mob);
        plugin.gui.setName(spawnedEntity);
        plugin.mobManager.giveMobGear(spawnedEntity, false);
        plugin.addHealth(mob);
        return mob;
    }

    /**
     * Change the given entity into infernal mob
     *
     * @param e     the entity
     * @param fixed
     */
    public void makeInfernal(final Entity e, final boolean fixed) {
        final boolean mobEnabled = true;
        String entName = e.getType().name();
        if (!e.hasMetadata("NPC") && !e.hasMetadata("shopkeeper")) {
            if (!fixed) {
                final ArrayList<String> babyList = (ArrayList<String>) plugin.getConfig().getList("disabledBabyMobs");
                if (e.getType().equals((Object) EntityType.MUSHROOM_COW)) {
                    final MushroomCow minion = (MushroomCow) e;
                    if (!minion.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.COW)) {
                    final Cow minion2 = (Cow) e;
                    if (!minion2.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.SHEEP)) {
                    final Sheep minion3 = (Sheep) e;
                    if (!minion3.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.PIG)) {
                    final Pig minion4 = (Pig) e;
                    if (!minion4.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.CHICKEN)) {
                    final Chicken minion5 = (Chicken) e;
                    if (!minion5.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.WOLF)) {
                    final Wolf minion6 = (Wolf) e;
                    if (!minion6.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.ZOMBIE)) {
                    final Zombie minion7 = (Zombie) e;
                    if (!minion7.isBaby() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.PIG_ZOMBIE)) {
                    final PigZombie minion8 = (PigZombie) e;
                    if (!minion8.isBaby() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.OCELOT)) {
                    final Ocelot minion9 = (Ocelot) e;
                    if (!minion9.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.HORSE)) {
                    final Horse minion10 = (Horse) e;
                    if (!minion10.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.VILLAGER)) {
                    final Villager minion11 = (Villager) e;
                    if (!minion11.isAdult() && babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.RABBIT)) {
                    final Rabbit minion12 = (Rabbit) e;
                    if (!minion12.isAdult() && babyList.contains(entName)) {
                        return;
                    }
                }
            }
            final UUID id = e.getUniqueId();
            final int chance = plugin.getConfig().getInt("chance");
            final boolean mobEnabled2 = mobEnabled;
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) plugin, (Runnable) new Runnable() {
                @Override
                public void run() {
                    String entName = e.getType().name();
                    if (!e.isDead() && e.isValid() && ((plugin.getConfig().getList("enabledmobs").contains(entName) && mobEnabled2) ||
                            (fixed))) {
                        final int min = 1;
                        int max = chance;
                        final int mc = plugin.getConfig().getInt("mobChances." + entName);
                        if (mc > 0) {
                            max = mc;
                        }
                        if (fixed) {
                            max = 1;
                        }
                        final int randomNum = Helper.rand(min, max);
                        if (randomNum == 1) {
                            final ArrayList<String> aList = plugin.getAbilitiesAmount(e);
                            if (plugin.getConfig().getString("levelChance." + aList.size()) != null) {
                                final int sc = plugin.getConfig().getInt("levelChance." + aList.size());
                                final int randomNum2 = new Random().nextInt(sc - min + 1) + min;
                                if (randomNum2 != 1) {
                                    return;
                                }
                            }
                            Mob newMob = null;
                            if (aList.contains("1up")) {
                                newMob = new Mob(e, id, e.getWorld(), true, aList, 2, plugin.getEffect());
                            } else {
                                newMob = new Mob(e, id, e.getWorld(), true, aList, 1, plugin.getEffect());
                            }
                            if (aList.contains("flying")) {
                                plugin.mobManager.makeFly(e);
                            }
                            mobMap.put(id, newMob);
                            plugin.gui.setName(e);
                            plugin.mobManager.giveMobGear(e, true);
                            plugin.addHealth(newMob);
                            if (plugin.getConfig().getBoolean("enableSpawnMessages")) {
                                if (plugin.getConfig().getList("spawnMessages") != null) {
                                    final ArrayList<String> spawnMessageList = (ArrayList<String>) plugin.getConfig().getList("spawnMessages");
                                    final Random randomGenerator = new Random();
                                    final int index = randomGenerator.nextInt(spawnMessageList.size());
                                    String spawnMessage = spawnMessageList.get(index);
                                    spawnMessage = ChatColor.translateAlternateColorCodes('&', spawnMessage);
                                    if (((LivingEntity) e).getCustomName() != null) {
                                        spawnMessage = spawnMessage.replace("mob", ((LivingEntity) e).getCustomName());
                                    } else {
                                        spawnMessage = spawnMessage.replace("mob", e.getType().toString().toLowerCase());
                                    }
                                    final int r = plugin.getConfig().getInt("spawnMessageRadius");
                                    if (r == -1) {
                                        for (final Player p : e.getWorld().getPlayers()) {
                                            p.sendMessage(spawnMessage);
                                        }
                                    } else if (r == -2) {
                                        Bukkit.broadcastMessage(spawnMessage);
                                    } else {
                                        for (final Entity e : e.getNearbyEntities((double) r, (double) r, (double) r)) {
                                            if (e instanceof Player) {
                                                final Player p2 = (Player) e;
                                                p2.sendMessage(spawnMessage);
                                            }
                                        }
                                    }
                                } else {
                                    System.out.println("No valid spawn messages found!");
                                }
                            }
                        }
                    }
                }
            }, 10L);
        }
    }

    public void giveMobGear(final Entity mob, final boolean naturalSpawn) {
        final UUID mobId = mob.getUniqueId();
        if (!mobMap.containsKey(mobId)) return;
        ArrayList<String> mobAbilityList = mobMap.get(mobId).abilityList;
        boolean armoured = false;
        if (mobAbilityList.contains("armoured")) {
            armoured = true;
            ((LivingEntity) mob).setCanPickupItems(false);
        }
        final ItemStack helm = new ItemStack(Material.DIAMOND_HELMET, 1);
        final ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        final ItemStack pants = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        final ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
        final ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
        final EntityEquipment ee = ((LivingEntity) mob).getEquipment();
        if (mob instanceof Skeleton) {
            final Skeleton sk = (Skeleton) mob;
            if (sk.getSkeletonType().equals((Object) Skeleton.SkeletonType.WITHER)) {
                if (armoured) {
                    ee.setHelmetDropChance(0.0f);
                    ee.setChestplateDropChance(0.0f);
                    ee.setLeggingsDropChance(0.0f);
                    ee.setBootsDropChance(0.0f);
                    ee.setItemInHandDropChance(0.0f);
                    ee.setHelmet(helm);
                    ee.setChestplate(chest);
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                    ee.setItemInHand(sword);
                }
            } else {
                final ItemStack bow = new ItemStack(Material.BOW, 1);
                ee.setItemInHand(bow);
                if (armoured) {
                    ee.setHelmetDropChance(0.0f);
                    ee.setChestplateDropChance(0.0f);
                    ee.setHelmet(helm);
                    ee.setChestplate(chest);
                    if (!mobAbilityList.contains("cloaked")) {
                        ee.setLeggingsDropChance(0.0f);
                        ee.setBootsDropChance(0.0f);
                        ee.setLeggings(pants);
                        ee.setBoots(boots);
                    }
                    ee.setItemInHandDropChance(0.0f);
                    ee.setItemInHand(sword);
                } else if (mobAbilityList.contains("cloaked")) {
                    final ItemStack skull = new ItemStack(Material.GLASS_BOTTLE, 1);
                    ee.setHelmet(skull);
                }
            }
        } else if (mob instanceof Zombie || mob instanceof PigZombie) {
            if (armoured) {
                ee.setHelmetDropChance(0.0f);
                ee.setChestplateDropChance(0.0f);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                if (!mobAbilityList.contains("cloaked")) {
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                }
                ee.setLeggingsDropChance(0.0f);
                ee.setBootsDropChance(0.0f);
                ee.setItemInHandDropChance(0.0f);
                ee.setItemInHand(sword);
            } else if (mob instanceof Zombie && mobAbilityList.contains("cloaked")) {
                final ItemStack skull2 = new ItemStack(Material.GLASS_BOTTLE, 1, (short) 2);
                ee.setHelmet(skull2);
            }
        }
        if ((mobAbilityList.contains("mounted") && plugin.getConfig().getList("enabledRiders").contains(mob.getType().getName())) || (!naturalSpawn && mobAbilityList.contains("mounted"))) {
            ArrayList<String> mounts = new ArrayList<String>();
            mounts = (ArrayList<String>) plugin.getConfig().getList("enabledMounts");
            final Random randomGenerator = new Random();
            final int index = randomGenerator.nextInt(mounts.size());
            String mount = mounts.get(index);
            String type = null;
            if (mount.contains(":")) {
                final String[] s = mount.split(":");
                mount = s[0];
                type = s[1];
            }
            if (EntityType.fromName(mount) != null) {
                final Entity liveMount = mob.getWorld().spawnEntity(mob.getLocation(), EntityType.fromName(mount));
                plugin.mountList.put(liveMount, mob);
                liveMount.setPassenger(mob);
                if (liveMount.getType().equals((Object) EntityType.HORSE)) {
                    final Horse hm = (Horse) liveMount;
                    if (type != null) {
                        hm.setVariant(Horse.Variant.valueOf(type));
                    } else {
                        final int randomNum2 = Helper.rand(1, 7);
                        if (randomNum2 <= 3) {
                            hm.setVariant(Horse.Variant.HORSE);
                        } else if (randomNum2 == 4) {
                            hm.setVariant(Horse.Variant.DONKEY);
                        } else if (randomNum2 == 5) {
                            hm.setVariant(Horse.Variant.MULE);
                        } else if (randomNum2 == 6) {
                            hm.setVariant(Horse.Variant.SKELETON_HORSE);
                        } else {
                            hm.setVariant(Horse.Variant.UNDEAD_HORSE);
                        }
                    }
                    if (plugin.getConfig().getBoolean("horseMountsHaveSaddles")) {
                        final ItemStack saddle = new ItemStack(Material.SADDLE);
                        hm.getInventory().setSaddle(saddle);
                    }
                    hm.setTamed(true);
                    if (hm.getVariant().equals((Object) Horse.Variant.HORSE)) {
                        final int randomNum3 = Helper.rand(1, 7);
                        if (randomNum3 == 1) {
                            hm.setColor(Horse.Color.BLACK);
                        } else if (randomNum3 == 2) {
                            hm.setColor(Horse.Color.BROWN);
                        } else if (randomNum3 == 3) {
                            hm.setColor(Horse.Color.CHESTNUT);
                        } else if (randomNum3 == 4) {
                            hm.setColor(Horse.Color.CREAMY);
                        } else if (randomNum3 == 5) {
                            hm.setColor(Horse.Color.DARK_BROWN);
                        } else if (randomNum3 == 6) {
                            hm.setColor(Horse.Color.GRAY);
                        } else {
                            hm.setColor(Horse.Color.WHITE);
                        }
                        if (armoured && plugin.getConfig().getBoolean("armouredMountsHaveArmour")) {
                            final ItemStack armour = new ItemStack(419);
                            hm.getInventory().setArmor(armour);
                        }
                    }
                } else if (liveMount.getType().equals((Object) EntityType.SHEEP)) {
                    final Sheep sh = (Sheep) liveMount;
                    if (type != null) {
                        sh.setColor(DyeColor.valueOf(type));
                    }
                }
            } else {
                System.out.println("Can't spawn mount!");
                System.out.println(String.valueOf(mount) + " is not a valid Entity!");
            }
        }
    }

    public void makeFly(final Entity ent) {
        final Entity bat = ent.getWorld().spawnEntity(ent.getLocation(), EntityType.BAT);
        bat.setVelocity(new Vector(0, 1, 0));
        bat.setPassenger(ent);
        ((LivingEntity) bat).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1), true);
    }

    /**
     * Spawn a ghost infernalMob
     */
    public void spawnGhost(final Location l) {
        boolean evil = false;
        if (new Random().nextInt(3) == 1) {
            evil = true;
        }
        final Zombie g = (Zombie) l.getWorld().spawnEntity(l, EntityType.ZOMBIE);
        g.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 199999980, 1));
        g.setCanPickupItems(false);
        final ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        ItemStack skull;
        if (evil) {
            skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
            Helper.changeLeatherColor(chest, Color.BLACK);
        } else {
            skull = new ItemStack(Material.SKULL_ITEM, 1);
            Helper.changeLeatherColor(chest, Color.WHITE);
        }
        chest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, new Random().nextInt(10) + 1);
        final ItemMeta m = skull.getItemMeta();
        m.setDisplayName("§fGhost Head");
        skull.setItemMeta(m);
        g.getEquipment().setHelmet(skull);
        g.getEquipment().setChestplate(chest);
        g.getEquipment().setHelmetDropChance(0.0f);
        g.getEquipment().setChestplateDropChance(0.0f);
        final int min = 1;
        final int max = 5;
        final int rn = new Random().nextInt(max - min + 1) + min;
        if (rn == 1) {
            g.getEquipment().setItemInHand(new ItemStack(Material.STONE_HOE, 1));
            g.getEquipment().setItemInHandDropChance(0.0f);
        }
        plugin.ghostMove((Entity) g);
        final ArrayList<String> aList = new ArrayList<String>();
        aList.add("ender");
        if (evil) {
            aList.add("necromancer");
            aList.add("withering");
            aList.add("blinding");
        } else {
            aList.add("ghastly");
            aList.add("sapper");
            aList.add("confusing");
        }
        Mob newMob;
        if (evil) {
            newMob = new Mob((Entity) g, g.getUniqueId(), g.getWorld(), false, aList, 1, "smoke:2:12");
        } else {
            newMob = new Mob((Entity) g, g.getUniqueId(), g.getWorld(), false, aList, 1, "cloud:0:8");
        }
        mobMap.put(newMob.id, newMob);
    }
}
