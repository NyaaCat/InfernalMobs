package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

class ArrowHomingTask extends BukkitRunnable {
    Arrow arrow;
    LivingEntity target;

    public ArrowHomingTask(final Arrow arrow, final LivingEntity target, final Plugin plugin) {
        this.arrow = arrow;
        this.target = target;
        this.runTaskTimer(plugin, 1L, 1L);
    }

    public void run() {
        try {
            final double speed = this.arrow.getVelocity().length();
            if (this.arrow.isOnGround() || this.arrow.isDead() || this.target.isDead()) {
                this.cancel();
                return;
            }
            final Vector toTarget = this.target.getLocation().clone().add(new Vector(0.0, 0.5, 0.0)).subtract(this.arrow.getLocation()).toVector();
            final Vector dirVelocity = this.arrow.getVelocity().clone().normalize();
            final Vector dirToTarget = toTarget.clone().normalize();
            final double angle = dirVelocity.angle(dirToTarget);
            final double newSpeed = 0.9 * speed + 0.14;
            Vector newVelocity;
            if (angle < 0.12) {
                newVelocity = dirVelocity.clone().multiply(newSpeed);
            } else {
                final Vector newDir = dirVelocity.clone().multiply((angle - 0.12) / angle).add(dirToTarget.clone().multiply(0.12 / angle));
                newDir.normalize();
                newVelocity = newDir.clone().multiply(newSpeed);
            }
            this.arrow.setVelocity(newVelocity.add(new Vector(0.0, 0.03, 0.0)));
        } catch (Exception ex) {
        }
    }
}
