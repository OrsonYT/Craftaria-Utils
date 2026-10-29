package fr.craftaria.utils;

import java.util.*;

/** Keeps the initial roster separate from later arrivals and departures. */
public final class ConnectionRoster {
    public enum Kind { JOINED, LEFT, ALREADY_ONLINE }
    public record Notice(String name,Kind kind) { }
    private Object connection;
    private Map<UUID,String> previous=Map.of();
    private int warmup;
    private boolean initialized;
    public void reset() { connection=null; previous=Map.of(); warmup=0; initialized=false; }
    public List<Notice> update(Object session,Map<UUID,String> players,Set<String> tracked,
                               boolean enabled,boolean announceInitial,boolean ready) {
        if(session!=connection) { connection=session; previous=Map.of(); warmup=60; initialized=false; }
        List<Notice> notices=new ArrayList<>();
        if(warmup>0) warmup--;
        else if(!initialized) {
            if(ready) {
                initialized=true;
                if(enabled && announceInitial) for(String name:players.values())
                    if(tracked.contains(name.toLowerCase(Locale.ROOT))) notices.add(new Notice(name,Kind.ALREADY_ONLINE));
            }
        } else if(enabled) {
            for(var entry:players.entrySet()) if(!previous.containsKey(entry.getKey()) && tracked.contains(entry.getValue().toLowerCase(Locale.ROOT)))
                notices.add(new Notice(entry.getValue(),Kind.JOINED));
            for(var entry:previous.entrySet()) if(!players.containsKey(entry.getKey()) && tracked.contains(entry.getValue().toLowerCase(Locale.ROOT)))
                notices.add(new Notice(entry.getValue(),Kind.LEFT));
        }
        previous=Map.copyOf(players);
        return notices;
    }
}
