package com.jacob_vejvoda.infernal_mobs.config;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import com.jacob_vejvoda.infernal_mobs.ability.EnumAbilities;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class AbilityConfig extends FileConfigure {
    @Serializable
    Map<String, Attr> abilities = new HashMap<>();

    @Override
    protected String getFileName() {
        return "abilities.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return InfernalMobs.instance;
    }

    public List<EnumAbilities> getConfigFor(int level) {
        List<EnumAbilities> abilities = new ArrayList<>();
        List<Map.Entry<String, Attr>> collect = this.abilities.entrySet().stream().filter(stringAttrEntry -> stringAttrEntry.getValue().matchLevel(level))
                .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            collect.forEach(stringAttrEntry -> {
                String key = stringAttrEntry.getKey();
                try {
                    EnumAbilities enumAbilities = EnumAbilities.valueOf(key.toUpperCase());
                    abilities.add(enumAbilities);
                }catch (IllegalArgumentException e){
                    getPlugin().getLogger().log(Level.WARNING, "unknown ability " + key);
                }
            });
        }
        return abilities;
    }

    public Attr getAttrForAbility(String abilityName){
        return abilities.computeIfAbsent(abilityName.toLowerCase(), s -> new Attr());
    }

    public static class Attr implements ISerializable {
        @Serializable
        boolean enabled = false;
        @Serializable
        List<Object> levels = new ArrayList<>();
        @Serializable
        Map<String, Object> extras = new LinkedHashMap<>();

        boolean matchLevel(int level){
            if (levels.isEmpty())return false;
            if (!enabled) return false;
            boolean result = false;
            try {
                for (Object object : levels) {
                    if ((object instanceof Integer)) {
                        if (((int) object) == level) {
                            result = true;
                            break;
                        }
                    } else if (object instanceof String) {
                        String[] split = ((String) object).split("-");
                        if (split.length != 2) {
                            InfernalMobs.instance.getLogger().log(Level.SEVERE, "wrong config format " + ((String) object) + ".");
                            result = false;
                            break;
                        }
                        int lv = Integer.valueOf(split[0].replaceAll(" ", ""));
                        int rv = Integer.valueOf(split[1].replaceAll(" ", ""));
                        if (level >= lv && level <= rv) {
                            result = true;
                            break;
                        }
                    } else {
                        InfernalMobs.instance.getLogger().log(Level.SEVERE, "type " + object.getClass().getName() + " not implemented");
                        result = true;
                    }
                }
            } catch (Exception e){
                InfernalMobs.instance.getLogger().log(Level.SEVERE, "exception:" ,e);
            }
            return result;
        }

        public Object getExtra(String key){
            return extras.get(key);
        }

        public String getStringExtra(String key){
            Object o = extras.get(key);
            if (o instanceof String) return (String) o;
            return o.toString();
        }

        public Integer getIntExtra(String key){
            Object o = extras.get(key);
            if (o instanceof Number)return ((Number) o).intValue();
            else if (o instanceof String) return Integer.parseInt((String) o);
            throw new ClassCastException("key "+key+" of class "+o.getClass().toString()+" can't cast to int");
        }

        public Double getDoubleExtra(String key){
            Object o = extras.get(key);
            if (o instanceof Number)return ((Number) o).doubleValue();
            else if (o instanceof String) return Double.parseDouble((String) o);
            throw new ClassCastException("value for key "+key+" of class "+o.getClass().toString()+" can't cast to int");
        }

        public void putExtra(String key, Object value) {
            extras.put(key,value);
        }

        public boolean hasExtra() {
            return !extras.isEmpty();
        }
    }
}
