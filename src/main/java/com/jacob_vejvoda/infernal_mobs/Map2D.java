package com.jacob_vejvoda.infernal_mobs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Map2D<TRow, TColumn, TElement> {
    private Set<TColumn> columnSet = new HashSet<>();
    private Map<TRow, Map<TColumn, TElement>> map = new HashMap<>();

    public void put(TRow r, TColumn c, TElement e) {
        if (!map.containsKey(r)) map.put(r, new HashMap<>());
        map.get(r).put(c, e);
        columnSet.add(c);
    }

    public TElement get(TRow r, TColumn c) {
        if (!map.containsKey(r)) return null;
        return map.get(r).get(c);
    }

    public Set<TRow> rowKeys() {
        return new HashSet<>(map.keySet());
    }

    public Set<TColumn> columnKeys() {
        return new HashSet<>(columnSet);
    }

    // May leads to redundant columnKey left in columnSet
    public void setRow(TRow r, Map<TColumn, TElement> row) {
        map.put(r, row);
        columnSet.addAll(row.keySet());
    }

    public Map<TRow, TElement> getColumn(TColumn c) {
        Map<TRow, TElement> ret = new HashMap<>();
        for (TRow r : map.keySet()) {
            if (map.get(r).containsKey(c)) {
                ret.put(r, map.get(r).get(c));
            }
        }
        return ret;
    }
}
