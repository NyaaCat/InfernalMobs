package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.Helper;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class AbilityPotions implements IAbility {
    private static final List<PotionEffect> POTION_EFFECTS = new ArrayList<PotionEffect>() {{
        add(new PotionEffect(PotionEffectType.HARM, 1, 2, false, true, Color.fromRGB(4393481)));
        add(new PotionEffect(PotionEffectType.HARM, 1, 1, false, true, Color.fromRGB(4393481)));
        add(new PotionEffect(PotionEffectType.POISON, 100, 1, false, true, Color.fromRGB(5149489)));
        add(new PotionEffect(PotionEffectType.SLOW, 100, 1, false, true, Color.fromRGB(5926017)));
        add(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1, false, true, Color.fromRGB(4738376)));
    }};

    @Override
    public void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {
        if (Helper.possibility(0.3)) return;
        Vector velocity = attacker.getEyeLocation().toVector().subtract(mobEntity.getEyeLocation().toVector());
        velocity.multiply(1D/15D);
        velocity.add(new Vector(0,0.2,0));
        ThrownPotion t = mobEntity.launchProjectile(ThrownPotion.class, velocity);

        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta pm = (PotionMeta) item.getItemMeta();
        PotionEffect effect = Helper.randomItem(POTION_EFFECTS);
        pm.addCustomEffect(effect, false);
        pm.setColor(effect.getColor());
        item.setItemMeta(pm);

        t.setItem(item);
    }
}
