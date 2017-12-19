package com.jacob_vejvoda.infernal_mobs.api;

import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Cannot be cancelled.
 */
public class InfernalMobSpawnEvent extends Event{
    private static HandlerList handlers = new HandlerList();
    public final LivingEntity mobEntity;
    public final Mob mob;
    public final UUID parentId;
    public final InfernalSpawnReason reason;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public InfernalMobSpawnEvent(LivingEntity mobEntity, Mob mob, UUID parentId, InfernalSpawnReason reason) {
        this.mobEntity = mobEntity;
        this.mob = mob;
        this.parentId = parentId;
        this.reason = reason;
    }
}
