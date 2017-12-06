package com.jacob_vejvoda.infernal_mobs.persist;

import com.jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleEffect {
    public final Particle particle;
    public final int speed;
    public final int amount;
    public ParticleEffect(Particle p, int s, int a) {
        this.particle = p;
        this.speed = s;
        this.amount = a;
    }

    @Override
    public String toString() {
        return String.format("%s:%d:%d", particle.name(), speed, amount);
    }

    public static ParticleEffect parse(String effect) {
        try {
            final String[] split = effect.split(":");
            Particle p = Particle.valueOf(split[0]);
            final int data1 = Integer.parseInt(split[1]);
            final int data2 = Integer.parseInt(split[2]);
            return new ParticleEffect(p, data1, data2);
        } catch (Exception ex) {
            InfernalMobs.instance.getLogger().warning("Invalid particle effect: " + effect);
            return new ParticleEffect(Particle.LAVA, 1, 10);
        }
    }

    public void spawnAt(Location loc) {
        loc.getWorld().spawnParticle(particle, loc, amount, 0D, 0D, 0D, (double)speed);
    }
}
