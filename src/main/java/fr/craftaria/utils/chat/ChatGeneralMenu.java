package fr.craftaria.utils.chat;

import net.minecraft.item.Items;
import net.minecraft.text.Text;
import java.util.List;
import java.util.function.Consumer;

public final class ChatGeneralMenu extends ChatActionMenu {
    public ChatGeneralMenu(Consumer<String> action) {
        super(144,88,Text.translatable("craftaria_utils.general.title"),Items.NETHER_STAR,ChatGeneralMenu::entries,action);
    }
    private static List<Action> entries() {
        return List.of(
                Action.command("shop","craftaria_utils.general.shop",Items.CHEST),
                Action.command("menu","craftaria_utils.general.menu",Items.NETHER_STAR),
                Action.command("ec","craftaria_utils.general.ec",Items.ENDER_CHEST),
                Action.command("quete","craftaria_utils.general.quete",Items.WRITABLE_BOOK),
                Action.command("skill","craftaria_utils.general.skill",Items.EXPERIENCE_BOTTLE),
                Action.command("skill top","craftaria_utils.general.skill_top",Items.GOLDEN_HELMET),
                Action.command("dex","craftaria_utils.general.dex",Items.BOOK),
                Action.command("profil","craftaria_utils.general.profil",Items.PLAYER_HEAD),
                Action.command("kit","craftaria_utils.general.kit",Items.BUNDLE),
                Action.command("stats","craftaria_utils.general.stats",Items.PAPER),
                Action.command("flytime","craftaria_utils.general.flytime",Items.ELYTRA),
                Action.command("wiki","craftaria_utils.general.wiki",Items.KNOWLEDGE_BOOK),
                Action.command("bossbar","craftaria_utils.general.bossbar",Items.DRAGON_HEAD),
                Action.command("sb","craftaria_utils.general.sb",Items.OAK_SIGN),
                Action.command("b","craftaria_utils.general.b",Items.POPPY));
    }
}
