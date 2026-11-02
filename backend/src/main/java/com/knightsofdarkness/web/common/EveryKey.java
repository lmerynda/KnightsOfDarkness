package com.knightsofdarkness.web.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * a count map that always carries a value for every key of its enum, so clients may read any key without a fallback;
 * a map without it carries only some keys
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EveryKey {
}
