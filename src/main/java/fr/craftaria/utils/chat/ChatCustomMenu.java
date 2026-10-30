package fr.craftaria.utils.chat;

import fr.craftaria.utils.config.ModConfig;
import fr.craftaria.utils.config.QuickHome;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ChatCustomMenu extends ChatActionMenu {
    public ChatCustomMenu(Consumer<String> action) {
        super(236,112,Text.translatable("craftaria_utils.custom_commands.button"),Items.COMMAND_BLOCK,ChatCustomMenu::entries,action);
    }
    private static List<Action> entries() {
        List<Action> entries=new ArrayList<>();
        for (var command:ModConfig.get().customCommands)
            entries.add(new Action("custom:run:"+command.name,Text.literal(command.label()),QuickHome.iconStack(command.icon),
                    Text.literal("/"+command.name),"/"+command.name));
        entries.add(new Action("custom:add",Text.translatable("craftaria_utils.custom_commands.add"),Items.WRITABLE_BOOK.getDefaultStack(),
                Text.translatable("craftaria_utils.custom_commands.description"),"+"));
        return entries;
    }
}
