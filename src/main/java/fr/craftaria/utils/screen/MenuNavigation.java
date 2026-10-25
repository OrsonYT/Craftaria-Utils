package fr.craftaria.utils.screen;

/** The menu explicitly chosen by the player for this game session. */
public final class MenuNavigation {
    private static boolean vanillaMenuSelected;
    private MenuNavigation() { }
    public static boolean isVanillaMenuSelected() { return vanillaMenuSelected; }
    public static void selectVanilla() { vanillaMenuSelected = true; }
    public static void selectCustom() { vanillaMenuSelected = false; }
}
