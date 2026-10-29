import fr.craftaria.utils.ConnectionRoster;
import fr.craftaria.utils.TrackedPlayers;
import fr.craftaria.utils.protection.AntiDropPolicy;
import java.util.*;
import static fr.craftaria.utils.protection.AntiDropPolicy.Destination.*;

/** Run with javac/java; requires no game, server, accounts, or inventory changes. */
public final class ProtectionAndRosterChecks {
    private static int checks;
    private static void check(boolean condition,String description) {
        if(!condition) throw new AssertionError(description);
        checks++;
    }
    private static List<String> joins(ConnectionRoster roster,Object session,Map<UUID,String> players,Set<String> tracked,boolean enabled) {
        return roster.update(session,players,tracked,enabled,false,true).stream()
                .filter(notice->notice.kind()==ConnectionRoster.Kind.JOINED).map(ConnectionRoster.Notice::name).toList();
    }
    public static void main(String[] args) {
        check(AntiDropPolicy.blocks("THROW",PLAYER,true,false,false,true),"Q on protected slot");
        check(!AntiDropPolicy.blocks("THROW",PLAYER,false,false,false,true),"Ordinary Q remains allowed");
        check(AntiDropPolicy.blocks("PICKUP",OUTSIDE,false,true,false,true),"Drop protected cursor outside inventory");
        check(AntiDropPolicy.blocks("PICKUP",CONTAINER,false,true,false,false),"Place protected cursor in chest");
        check(!AntiDropPolicy.blocks("PICKUP",PLAYER,false,true,false,false),"Move protected cursor within player inventory");
        check(AntiDropPolicy.blocks("QUICK_MOVE",PLAYER,true,false,false,false),"Shift-click protected item into chest");
        check(!AntiDropPolicy.blocks("QUICK_MOVE",PLAYER,true,false,false,true),"Shift-click within standalone player inventory");
        check(!AntiDropPolicy.blocks("QUICK_MOVE",CONTAINER,true,false,false,false),"Take a protected type out of chest");
        check(AntiDropPolicy.blocks("SWAP",CONTAINER,false,false,true,false),"Number-key or offhand exchange into chest");
        check(!AntiDropPolicy.blocks("SWAP",PLAYER,true,false,true,false),"Number-key rearrangement within player inventory");
        check(AntiDropPolicy.blocks("QUICK_CRAFT",CONTAINER,false,true,false,false),"Dragging protected cursor into chest");
        check(!AntiDropPolicy.blocks("QUICK_CRAFT",PLAYER,false,true,false,false),"Dragging within inventory remains allowed");
        check(!AntiDropPolicy.blocks("QUICK_CRAFT",OUTSIDE,false,true,false,false),"Drag start/end packets remain paired");
        check(!AntiDropPolicy.blocks("PICKUP",CONTAINER,false,false,false,false),"Unprotected items remain usable");
        check(TrackedPlayers.parse("OrsonYT_").equals(List.of("OrsonYT_")),"Username containing n must not split");
        check(TrackedPlayers.parse("Nathan, nicolas; ANN_\nSteve").equals(List.of("Nathan","nicolas","ANN_","Steve")),"Separators preserve usernames");
        check(TrackedPlayers.parse("Steve, steve, STEVE").equals(List.of("Steve")),"Case-insensitive tracked username deduplication");
        check(TrackedPlayers.parse("abc invalid! ; good_name").equals(List.of("abc","good_name")),"Invalid names filtered without corrupting other names");
        check(TrackedPlayers.parse(null).isEmpty(),"Missing config remains readable");
        var roster=new ConnectionRoster(); Object session=new Object(); UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        Map<UUID,String> players=new HashMap<>(); players.put(a,"Alice");
        for(int i=0;i<=60;i++) check(joins(roster,session,players,Set.of("alice","bob"),true).isEmpty(),"Initial roster must stay quiet");
        players.put(b,"Bob"); check(joins(roster,session,players,Set.of("alice","bob"),true).equals(List.of("Bob")),"Tracked arrival detected by UUID");
        check(joins(roster,session,players,Set.of("alice","bob"),true).isEmpty(),"No repeated notification");
        players.remove(b); check(joins(roster,session,players,Set.of("bob"),true).isEmpty(),"Leaving is not a join");
        players.put(b,"BOB"); check(joins(roster,session,players,Set.of("bob"),true).equals(List.of("BOB")),"Reconnect and case-insensitive friend match");
        players.put(UUID.randomUUID(),"Other"); check(joins(roster,session,players,Set.of("bob"),true).isEmpty(),"Untracked player ignored");
        players.remove(b); joins(roster,session,players,Set.of("bob"),false); players.put(b,"Bob");
        check(joins(roster,session,players,Set.of("bob"),false).isEmpty(),"Disabled tracker is silent");
        check(joins(roster,session,players,Set.of("bob"),true).isEmpty(),"Enabling does not announce already online friends");
        check(joins(roster,new Object(),players,Set.of("bob"),true).isEmpty(),"New server resets initial baseline");
        var realNames=new ConnectionRoster(); Object currentSession=new Object(); UUID orson=UUID.randomUUID();
        Set<String> watched=new HashSet<>();
        for(String name:TrackedPlayers.parse("OrsonYT_")) watched.add(name.toLowerCase(Locale.ROOT));
        for(int i=0;i<=60;i++) joins(realNames,currentSession,Map.of(),watched,true);
        check(joins(realNames,currentSession,Map.of(orson,"OrsonYT_"),watched,true).equals(List.of("OrsonYT_")),"Persisted real username must trigger roster notification");
        var alerts=new ConnectionRoster(); Object alertSession=new Object();
        var friends=Set.of("alice","bob","charlie"); UUID charlie=UUID.randomUUID();
        Map<UUID,String> initial=new HashMap<>(Map.of(a,"Alice",b,"Bob"));
        for(int i=0;i<120;i++) check(alerts.update(alertSession,initial,friends,true,true,false).isEmpty(),"No notification during resource application");
        var initialNotices=alerts.update(alertSession,initial,friends,true,true,true);
        check(initialNotices.size()==2 && initialNotices.stream().allMatch(n->n.kind()==ConnectionRoster.Kind.ALREADY_ONLINE),"Announce online friends after resources finish");
        check(alerts.update(alertSession,initial,friends,true,true,true).isEmpty(),"Already online announced once only");
        initial.remove(a);
        check(alerts.update(alertSession,initial,friends,true,true,true).equals(List.of(new ConnectionRoster.Notice("Alice",ConnectionRoster.Kind.LEFT))),"Departure keeps the missing player's name");
        initial.put(charlie,"Charlie");
        check(alerts.update(alertSession,initial,friends,true,true,false).equals(List.of(new ConnectionRoster.Notice("Charlie",ConnectionRoster.Kind.JOINED))),"Live arrivals can be queued while resources reload");
        check(alerts.update(alertSession,initial,friends,true,true,true).isEmpty(),"Reload does not reannounce initial list");
        var noInitial=new ConnectionRoster(); Object noInitialSession=new Object();
        for(int i=0;i<61;i++) check(noInitial.update(noInitialSession,initial,friends,true,false,true).isEmpty(),"Initial option off remains silent");
        System.out.println(checks+" protection and roster checks passed");
    }
}
