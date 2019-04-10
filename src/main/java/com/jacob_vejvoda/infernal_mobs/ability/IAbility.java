package com.jacob_vejvoda.infernal_mobs.ability;

import com.jacob_vejvoda.infernal_mobs.ConfigReader;
import com.jacob_vejvoda.infernal_mobs.api.InfernalMobSpawnEvent;
import com.jacob_vejvoda.infernal_mobs.config.AbilityConfig;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface IAbility {
    default void readExtra(String abilityName) {
        List<Field> properties = getProperties(this.getClass());
        AbilityConfig.Attr attrForAbility = ConfigReader.getAbilityConfig().getAttrForAbility(abilityName);
        if (properties.isEmpty())return;
        try {
            if (!attrForAbility.hasExtra()) {
                this.init(abilityName, properties);
            }
            for (Field field : properties) {
                Property annotation = field.getAnnotation(Property.class);
                if (annotation != null) {
                    String name = field.getName();
                    field.setAccessible(true);
                    Object o = field.get(this);
                    Object extra = attrForAbility.getExtra(name);
                    field.set(this, extra == null ? o : extra);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    default List<Field> getProperties(Class targetClass){
        List<Field> fields = new ArrayList<>();
        Class superclass = targetClass.getSuperclass();
        if (superclass!=null){
             fields.addAll(getProperties(superclass));
        }
        Field[] declaredFields = targetClass.getDeclaredFields();
        List<Field> collect = Arrays.stream(declaredFields).filter(field -> field.getAnnotation(Property.class) != null).collect(Collectors.toList());
        fields.addAll(collect);
        return fields;
    }

    default void init(String abilityName, List<Field> propertyList) throws IllegalAccessException {
        AbilityConfig.Attr attrForAbility = ConfigReader.getAbilityConfig().getAttrForAbility(abilityName);
        if (!propertyList.isEmpty()){
            propertyList.forEach(field -> {
                try {
                    Object o = field.get(IAbility.this);
                    attrForAbility.putExtra(field.getName(), o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
        ConfigReader.getAbilityConfig().save();
    }

    default void perCycleEffect(LivingEntity mobEntity, Mob mob) {}
    default void onMobSpawn(InfernalMobSpawnEvent ev) {}
    default void onDeath(LivingEntity mobEntity, Mob mob, Player killer, EntityDeathEvent ev) {}
    default void onPlayerAttack(LivingEntity mobEntity, Mob mob, Player attacker, boolean isDirectAttack, EntityDamageByEntityEvent ev) {}
    default void onAttackPlayer(LivingEntity mobEntity, Mob mob, Player victim, boolean isDirectAttack, EntityDamageByEntityEvent ev) {}
}
