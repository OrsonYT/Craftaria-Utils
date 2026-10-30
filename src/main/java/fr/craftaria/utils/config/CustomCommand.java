package fr.craftaria.utils.config;

/** A single server command, stored without the leading slash. */
public final class CustomCommand {
    public String name="", displayName="", icon="minecraft:command_block";
    public CustomCommand() { }
    public CustomCommand(String command,String label,String icon) { name=normalize(command); displayName=label; this.icon=icon; }
    public String label() { return displayName==null || displayName.isBlank()?"/"+name:displayName; }
    public static String normalize(String command) {
        if(command==null) return "";
        String value=command.strip();
        return value.startsWith("/")?value.substring(1):value;
    }
    public static boolean validName(String command) {
        String value=normalize(command);
        return !value.isBlank() && value.length()<=255 && !value.startsWith("/")
                && value.chars().noneMatch(Character::isISOControl);
    }
    public static net.minecraft.item.ItemStack iconStack(String icon) { return QuickHome.iconStack(icon); }
}
