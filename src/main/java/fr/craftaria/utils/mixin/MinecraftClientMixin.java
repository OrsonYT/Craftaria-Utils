package fr.craftaria.utils.mixin;

import fr.craftaria.utils.screen.CraftariaTitleScreen;
import fr.craftaria.utils.screen.MenuNavigation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen craftariaUtils$titleScreen(Screen screen) {
        if (!MenuNavigation.isVanillaMenuSelected() && screen != null
                && (screen.getClass() == TitleScreen.class || screen instanceof MultiplayerScreen)) {
            return new CraftariaTitleScreen();
        }
        return screen;
    }
}
