package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersistStorage {
    private final infernal_mobs plugin;
    private final File presistFile;

    // Map<SpawnerLocation, SpawnInterval>
    public final Map<Location, Integer> validInfernalSpawners = new HashMap<>();

    public PersistStorage(infernal_mobs plugin, File f) {
        this.plugin = plugin;
        this.presistFile = f;
    }

    public void loadToMemory() {
        if (!presistFile.exists()) {
            try {
                presistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(presistFile);
        if (cfg.isConfigurationSection("spawners")) {
            ConfigurationSection sec = cfg.getConfigurationSection("spawners");
            for (String key : sec.getKeys(false)) {
                validInfernalSpawners.put((Location) sec.get(key + ".location"), sec.getInt(key + ".interval"));
            }
        }
        if (cfg.isConfigurationSection("mobs")) {
            ConfigurationSection sec = cfg.getConfigurationSection("mobs");
            for (String key : sec.getKeys(false)) {
                Mob m = new Mob();
                m.id = UUID.fromString(sec.getString(key + ".uuid"));
                m.world = plugin.getServer().getWorld(sec.getString(key + ".world_name"));
                m.infernal = sec.getBoolean(key + ".infernal");
                m.lives = sec.getInt(key + ".lives");
                m.effect = sec.getString(key + ".effect");
                m.abilityList = new ArrayList<>();
                m.abilityList.addAll(sec.getStringList(key + ".abilities"));
                m.entity = plugin.getServer().getEntity(m.id);
                if (m.entity != null) {
                    plugin.mobManager.mobMap.put(m.id, m);
                }
            }
        }
    }

    public void saveToFile() {
        YamlConfiguration root = new YamlConfiguration();
        ConfigurationSection spSec = root.createSection("spawners");
        int idx = 0;
        for (Location l : validInfernalSpawners.keySet()) {
            ConfigurationSection sec = spSec.createSection(Integer.toString(idx));
            sec.set("location", l);
            sec.set("interval", validInfernalSpawners.get(l));
            idx++;
        }

        idx = 0;
        for (UUID id : plugin.mobManager.mobMap.keySet()) {
            ConfigurationSection sec = root.createSection("mobs." + Integer.toString(idx));
            Mob m = plugin.mobManager.mobMap.get(id);
            sec.set("uuid", id.toString());
            sec.set("world_name", m.world.getName());
            sec.set("infernal", m.infernal);
            sec.set("lives", m.lives);
            sec.set("effect", m.effect);
            sec.set("abilities", m.abilityList);
            idx++;
        }
        try {
            root.save(presistFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
