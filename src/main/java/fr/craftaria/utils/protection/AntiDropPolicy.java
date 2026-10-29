package fr.craftaria.utils.protection;

/** Decisions independent of inventory mutation, used by the network action guard. */
public final class AntiDropPolicy {
    public enum Destination { PLAYER, CONTAINER, OUTSIDE }
    private AntiDropPolicy() { }
    public static boolean blocks(String action,Destination destination,boolean protectedSource,
                                 boolean protectedCursor,boolean protectedSwap,boolean ownInventoryScreen) {
        return switch(action) {
            case "THROW" -> protectedSource;
            case "QUICK_MOVE" -> protectedSource && destination==Destination.PLAYER && !ownInventoryScreen;
            case "PICKUP" -> protectedCursor && destination!=Destination.PLAYER;
            case "QUICK_CRAFT" -> protectedCursor && destination==Destination.CONTAINER;
            case "SWAP" -> destination==Destination.CONTAINER && protectedSwap;
            default -> false;
        };
    }
}
