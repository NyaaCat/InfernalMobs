package com.jacob_vejvoda.infernal_mobs.persist;

import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Store runtime data such as:
 * - Spawned infernal mobs
 * - Location of infernal mob spawners
 * so they survive server reboots
 */
public class PersistStorage {
    private final InfernalMobs plugin;
    private final File persistFile;

    public final Map<Location, Integer> validInfernalSpawners = new HashMap<>(); // Map<SpawnerLocation, SpawnInterval>

    public PersistStorage(InfernalMobs plugin, File f) {
        this.plugin = plugin;
        this.persistFile = f;
    }

    public void loadToMemory() {
        if (!persistFile.exists()) {
            try {
                persistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(persistFile);
        if (cfg.isConfigurationSection("spawners")) {
            ConfigurationSection sec = cfg.getConfigurationSection("spawners");
            for (String key : sec.getKeys(false)) {
                validInfernalSpawners.put((Location) sec.get(key + ".location"), sec.getInt(key + ".interval"));
            }
        }
        if (cfg.isConfigurationSection("mobs")) {
            ConfigurationSection sec = cfg.getConfigurationSection("mobs");
            for (String key : sec.getKeys(false)) {
                List<EnumAbilities> abilities = new ArrayList<>();
                for (String str : sec.getStringList(key + ".abilities")) {
                    try {
                        abilities.add(EnumAbilities.valueOf(str));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                Mob m = new Mob(UUID.fromString(sec.getString(key + ".uuid")),
                        sec.getInt(key + ".lives"),
                        ParticleEffect.parse(sec.getString(key + ".effect")),
                        abilities);
                if (plugin.getServer().getEntity(m.entityId) != null) {
                    plugin.mobManager.mobMap.put(m.entityId, m);
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
            sec.set("lives", m.lives);

            List<String> abilities = new ArrayList<>();
            for (EnumAbilities a : m.abilityList) abilities.add(a.name());
            sec.set("abilities", abilities);
            sec.set("effect", m.particleEffect.toString());
            idx++;
        }
        try {
            root.save(persistFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
