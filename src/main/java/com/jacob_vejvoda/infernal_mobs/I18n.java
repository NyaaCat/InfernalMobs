package com.jacob_vejvoda.infernal_mobs;

import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.plugin.java.JavaPlugin;

public class I18n extends LanguageRepository {
    private static I18n INSTANCE;

    public I18n(){
        INSTANCE = this;
    }

    public static String format(String s, Object ... args) {
        return INSTANCE.getFormatted(s, args);
    }

    @Override
    protected JavaPlugin getPlugin() {
        return InfernalMobs.instance;
    }

    @Override
    protected String getLanguage() {
        return ConfigReader.getLanguage();
    }
}
