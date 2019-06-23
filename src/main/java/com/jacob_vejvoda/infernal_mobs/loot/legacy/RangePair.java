package com.jacob_vejvoda.infernal_mobs.loot.legacy;

import com.jacob_vejvoda.infernal_mobs.InfernalMobs;

/** An integer range, expressed as "{minInt}-{maxInt}" */
public class RangePair {
    int min;
    int max;

    public RangePair(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static RangePair parse(String str) {
        try {
            String[] arr = str.split("-");
            int min = Integer.parseInt(arr[0]);
            int max = Integer.parseInt(arr[1]);
            if (min < max) return new RangePair(min, max);
            else return new RangePair(max, min);
        } catch (Exception ex) {
            InfernalMobs.instance.getLogger().warning("Bad range pair:" + str);
            return null;
        }
    }

    public int get() {
        return LootManager.rnd.nextInt(max - min + 1) + min;
    }

    @Override
    public String toString() {
        return String.format("%d-%d", min, max);
    }
}
