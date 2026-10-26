package fr.craftaria.utils.config;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class QuickHome {
    /** The exact home argument sent to the server, for example "b". */
    public String name;
    /** Optional friendly label shown in Craftaria Utils, for example "Base". */
    public String displayName;
    public String icon = "minecraft:red_bed";
    public QuickHome() { }
    public QuickHome(String name, String displayName, String icon) { this.name=name; this.displayName=displayName; this.icon=icon; }
    public String label() { return displayName==null || displayName.isBlank() ? name : displayName; }
    public static boolean validName(String name) {
        return name!=null && !name.isBlank() && name.length()<=32
                && name.chars().noneMatch(c -> Character.isWhitespace(c) || Character.isISOControl(c) || c=='/' || c=='\\');
    }
    public static ItemStack iconStack(String icon) {
        Identifier id = icon==null ? null : Identifier.tryParse(icon);
        var item = id==null ? null : Registries.ITEM.get(id);
        return item==null || item==Items.AIR ? Items.RED_BED.getDefaultStack() : item.getDefaultStack();
    }
}
