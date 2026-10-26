package fr.craftaria.utils.chat;

import fr.craftaria.utils.config.ModConfig;
import fr.craftaria.utils.config.QuickHome;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ChatTeleportMenu extends ChatActionMenu {
    public ChatTeleportMenu(Consumer<String> action) {
        super(28,112,Text.translatable("craftaria_utils.teleport.title"),Items.ENDER_PEARL,ChatTeleportMenu::entries,action);
    }
    private static List<Action> entries() {
        List<Action> entries=new ArrayList<>(List.of(
                Action.command("tpa","craftaria_utils.teleport.tpa",Items.PLAYER_HEAD),
                Action.command("tpahere","craftaria_utils.teleport.tpahere",Items.ENDER_PEARL)));
        for (QuickHome home:ModConfig.get().quickHomes) if (home!=null && QuickHome.validName(home.name))
            entries.add(new Action("home "+home.name,Text.literal(home.label()),QuickHome.iconStack(home.icon),
                    Text.translatable("craftaria_utils.quick_homes.go",home.name),"/home "+home.name));
        for (QuickHome home:ModConfig.get().quickHomes) if (home!=null && QuickHome.validName(home.name))
            entries.add(new Action("quick:edit:"+home.name,Text.translatable("craftaria_utils.quick_homes.edit",home.label()),
                    QuickHome.iconStack(home.icon),Text.translatable("craftaria_utils.quick_homes.edit_description",home.name),"✎"));
        entries.addAll(List.of(
                Action.command("rtp","craftaria_utils.teleport.rtp",Items.COMPASS),
                Action.command("warp","craftaria_utils.teleport.warp",Items.ENDER_EYE),
                Action.command("home","craftaria_utils.teleport.home",Items.RED_BED),
                Action.command("spawn","craftaria_utils.teleport.spawn",Items.RESPAWN_ANCHOR),
                Action.command("casino","craftaria_utils.teleport.casino",Items.EMERALD),
                Action.command("crate","craftaria_utils.teleport.crate",Items.CHEST)));
        entries.add(new Action("quick:add",Text.translatable("craftaria_utils.quick_homes.add"),Items.NAME_TAG.getDefaultStack(),
                Text.translatable("craftaria_utils.quick_homes.description"),"+"));
        return entries;
    }
}
