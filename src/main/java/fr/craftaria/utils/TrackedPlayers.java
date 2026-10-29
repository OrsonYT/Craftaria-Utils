package fr.craftaria.utils;

import java.util.*;

/** One parser for settings and detection so a saved username is never split differently. */
public final class TrackedPlayers {
    private TrackedPlayers() { }
    public static List<String> parse(String value) {
        if(value==null || value.isBlank()) return List.of();
        Map<String,String> names=new LinkedHashMap<>();
        for(String name:value.split("[,;\\s]+")) {
            if(name.matches("[A-Za-z0-9_]{3,16}")) names.putIfAbsent(name.toLowerCase(Locale.ROOT),name);
        }
        return List.copyOf(names.values());
    }
}
