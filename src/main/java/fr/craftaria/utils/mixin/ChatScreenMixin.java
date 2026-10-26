package fr.craftaria.utils.mixin;

import fr.craftaria.utils.chat.ChatShareMenu;
import fr.craftaria.utils.chat.ChatTeleportMenu;
import fr.craftaria.utils.chat.ChatGeneralMenu;
import fr.craftaria.utils.CraftariaUtilsClient;
import fr.craftaria.utils.screen.TeleportPlayersScreen;
import fr.craftaria.utils.screen.QuickHomesScreen;
import fr.craftaria.utils.config.ModConfig;
import fr.craftaria.utils.config.QuickHome;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow protected TextFieldWidget chatField;
    @Shadow protected String originalChatText;
    @Unique private ChatShareMenu craftariaUtils$shareMenu;
    @Unique private ChatTeleportMenu craftariaUtils$teleportMenu;
    @Unique private ChatGeneralMenu craftariaUtils$generalMenu;

    protected ChatScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void craftariaUtils$initShareMenu(CallbackInfo ci) {
        craftariaUtils$shareMenu = new ChatShareMenu(token -> {
            int cursor = chatField.getCursor();
            String draft = chatField.getText();
            String prefix = cursor > 0 && !Character.isWhitespace(draft.charAt(cursor-1)) ? " " : "";
            String insertion = prefix+token;
            int available = 256-(draft.length()-chatField.getSelectedText().length());
            if (insertion.length()>available) {
                client.inGameHud.setOverlayMessage(Text.translatable("craftaria_utils.chat.full"), false);
                return;
            }
            chatField.write(insertion+(available>insertion.length() ? " " : ""));
            originalChatText = chatField.getText();
            setFocused(chatField);
            chatField.setFocused(true);
        });
        craftariaUtils$shareMenu.layout(width, chatField.getY());
        craftariaUtils$teleportMenu = new ChatTeleportMenu(command -> {
            craftariaUtils$shareMenu.close();
            originalChatText = chatField.getText();
            if (command.equals("quick:add") || command.startsWith("quick:edit:")) {
                QuickHome selected = command.startsWith("quick:edit:") ? ModConfig.get().quickHomes.stream()
                        .filter(home -> home.name.equals(command.substring("quick:edit:".length()))).findFirst().orElse(null) : null;
                client.setScreen(new QuickHomesScreen((Screen)(Object)this));
                if (selected != null) ((QuickHomesScreen)client.currentScreen).editHome(selected);
            } else if (command.equals("tpa") || command.equals("tpahere")) {
                client.setScreen(new TeleportPlayersScreen((Screen)(Object)this,
                        command.equals("tpa") ? TeleportPlayersScreen.Mode.TPA : TeleportPlayersScreen.Mode.TPAHERE));
            } else {
                CraftariaUtilsClient.sendCommand(client, command);
                client.setScreen(null);
            }
        });
        craftariaUtils$teleportMenu.layout(width, chatField.getY());
        craftariaUtils$generalMenu = new ChatGeneralMenu(command -> {
            craftariaUtils$shareMenu.close();
            craftariaUtils$teleportMenu.close();
            originalChatText = chatField.getText();
            CraftariaUtilsClient.sendCommand(client, command);
            client.setScreen(null);
        });
        craftariaUtils$generalMenu.layout(width, chatField.getY());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void craftariaUtils$renderShareMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (craftariaUtils$shareMenu != null) {
            craftariaUtils$shareMenu.layout(width, chatField.getY());
            craftariaUtils$shareMenu.render(context, mouseX, mouseY);
        }
        if (craftariaUtils$teleportMenu != null) {
            craftariaUtils$teleportMenu.layout(width, chatField.getY());
            craftariaUtils$teleportMenu.render(context, mouseX, mouseY);
        }
        if (craftariaUtils$generalMenu != null) {
            craftariaUtils$generalMenu.layout(width, chatField.getY());
            craftariaUtils$generalMenu.render(context, mouseX, mouseY);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void craftariaUtils$clickShareMenu(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (craftariaUtils$generalMenu != null && craftariaUtils$generalMenu.mouseClicked(click.x(), click.y(), click.button())) {
            if (craftariaUtils$shareMenu != null) craftariaUtils$shareMenu.close();
            if (craftariaUtils$teleportMenu != null) craftariaUtils$teleportMenu.close();
            cir.setReturnValue(true);
            return;
        }
        if (craftariaUtils$teleportMenu != null && craftariaUtils$teleportMenu.mouseClicked(click.x(), click.y(), click.button())) {
            if (craftariaUtils$generalMenu != null) craftariaUtils$generalMenu.close();
            if (craftariaUtils$shareMenu != null) craftariaUtils$shareMenu.close();
            cir.setReturnValue(true);
            return;
        }
        if (craftariaUtils$shareMenu != null && craftariaUtils$shareMenu.mouseClicked(click.x(), click.y(), click.button())) {
            if (craftariaUtils$generalMenu != null) craftariaUtils$generalMenu.close();
            if (craftariaUtils$teleportMenu != null) craftariaUtils$teleportMenu.close();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void craftariaUtils$closeShareMenu(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        boolean menuOpen = (craftariaUtils$shareMenu != null && craftariaUtils$shareMenu.isOpen())
                || (craftariaUtils$teleportMenu != null && craftariaUtils$teleportMenu.isOpen())
                || (craftariaUtils$generalMenu != null && craftariaUtils$generalMenu.isOpen());
        if (menuOpen && input.key()==GLFW.GLFW_KEY_ESCAPE) {
            if (craftariaUtils$shareMenu != null) craftariaUtils$shareMenu.close();
            if (craftariaUtils$teleportMenu != null) craftariaUtils$teleportMenu.close();
            if (craftariaUtils$generalMenu != null) craftariaUtils$generalMenu.close();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void craftariaUtils$shareMenuScroll(double mouseX, double mouseY, double horizontal, double vertical,
                                              CallbackInfoReturnable<Boolean> cir) {
        if ((craftariaUtils$generalMenu != null && craftariaUtils$generalMenu.mouseScrolled(mouseX, mouseY, vertical))
                || (craftariaUtils$teleportMenu != null && craftariaUtils$teleportMenu.mouseScrolled(mouseX, mouseY, vertical))) {
            cir.setReturnValue(true);
            return;
        }
        if ((craftariaUtils$shareMenu != null && craftariaUtils$shareMenu.covers(mouseX, mouseY))
                || (craftariaUtils$teleportMenu != null && craftariaUtils$teleportMenu.covers(mouseX, mouseY))) cir.setReturnValue(true);
    }
}
