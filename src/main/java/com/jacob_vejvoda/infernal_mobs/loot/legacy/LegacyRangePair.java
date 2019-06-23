package com.jacob_vejvoda.infernal_mobs.loot.legacy;

import com.jacob_vejvoda.infernal_mobs.InfernalMobs;

/** An integer range, expressed as "{minInt}-{maxInt}" */
public class LegacyRangePair {
    int min;
    int max;

    public LegacyRangePair(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static LegacyRangePair parse(String str) {
        try {
            String[] arr = str.split("-");
            int min = Integer.parseInt(arr[0]);
            int max = Integer.parseInt(arr[1]);
            if (min < max) return new LegacyRangePair(min, max);
            else return new LegacyRangePair(max, min);
        } catch (Exception ex) {
            InfernalMobs.instance.getLogger().warning("Bad range pair:" + str);
            return null;
        }
    }

    public int get() {
        return LegacyLootManager.rnd.nextInt(max - min + 1) + min;
    }

    @Override
    public String toString() {
        return String.format("%d-%d", min, max);
    }
}
