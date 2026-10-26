package fr.craftaria.utils.mixin;

import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Screen.class)
public abstract class LoadingBackgroundMixin {
    /** Intercept the background dispatch so overridden terrain/portal backgrounds are covered too. */
    @Redirect(method = "renderWithTooltip", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V"))
    private void craftariaUtils$loadingBackground(Screen screen, DrawContext context, int mouseX, int mouseY, float delta) {
        if (screen instanceof LevelLoadingScreen || screen instanceof ReconfiguringScreen
                || screen instanceof ProgressScreen || screen instanceof MessageScreen || screen instanceof ConnectScreen) {
            WallpaperManager.draw(context, screen.width, screen.height);
            context.fill(0, 0, screen.width, screen.height, 0x60000000);
        } else {
            screen.renderBackground(context, mouseX, mouseY, delta);
        }
    }
}
