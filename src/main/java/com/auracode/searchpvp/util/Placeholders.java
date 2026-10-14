package com.auracode.searchpvp.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Small fluent helper for building MiniMessage placeholder maps.
 */
public final class Placeholders {

    private final Map<String, String> values = new HashMap<>();

    private Placeholders() {
    }

    public static Placeholders of() {
        return new Placeholders();
    }

    public Placeholders set(String key, Object value) {
        values.put(key, String.valueOf(value));
        return this;
    }

    public Map<String, String> build() {
        return values;
    }
}
