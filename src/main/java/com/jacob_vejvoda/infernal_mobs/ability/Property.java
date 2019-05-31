package com.jacob_vejvoda.infernal_mobs.ability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    String name() default "";
    Class type() default Object.class;
}
