package com.jacob_vejvoda.infernal_mobs.api;

public enum InfernalSpawnReason {
    NATURAL, // Naturally spawned
    SPAWNER, // Spawned by infernal spawner.
    COMMAND, // Spawned by command
    MAMA,    // Spawned by mama ability
    MORPH,   // Spawned by morph ability
    GHOST,   // Spawned by ghost ability
    API,     // Spawned via API
    ONEUP,   // Spawned by 1up ability
    OTHER;   // Other reason
}
