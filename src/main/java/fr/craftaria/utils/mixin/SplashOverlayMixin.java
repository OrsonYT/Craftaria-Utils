package fr.craftaria.utils.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Change the visuals only; vanilla still handles reload completion, errors, and fading. */
@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Shadow @Final private boolean reloading;
    @Shadow private long reloadCompleteTime;
    @Shadow private long reloadStartTime;
    @Unique private static final Identifier CRAFTARIA_LOGO = Identifier.of("craftaria_utils", "icon-transparent.png");

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void craftariaUtils$reloadBackground(DrawContext context, int x, int y, int right, int bottom, int color) {
        if (!reloading) {
            context.fill(x, y, right, bottom, color);
            return;
        }
        int alpha = color >>> 24;
        WallpaperManager.draw(context, x, y, right - x, bottom - y, alpha);
        context.fill(x, y, right, bottom, (Math.round(alpha * .40f) << 24));
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V"))
    private void craftariaUtils$startupLogoOnly(DrawContext context, RenderPipeline pipeline, Identifier texture,
            int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight,
            int textureWidth, int textureHeight, int color) {
        if (!reloading) context.drawTexture(pipeline, texture, x, y, u, v, width, height,
                regionWidth, regionHeight, textureWidth, textureHeight, color);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void craftariaUtils$reloadLabel(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!reloading) return;
        long now = Util.getMeasuringTimeMs();
        float alpha = Math.clamp((now - reloadStartTime) / 500f, 0f, 1f);
        if (reloadCompleteTime > -1) alpha *= 1f - Math.clamp((now - reloadCompleteTime) / 1000f, 0f, 1f);
        if (Math.round(alpha * 255) < 4) return;
        int color = (Math.round(alpha * 255) << 24) | 0xFFFFFF;
        int centerX = context.getScaledWindowWidth() / 2, centerY = context.getScaledWindowHeight() / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, CRAFTARIA_LOGO, centerX - 32, centerY - 44,
                0, 0, 64, 64, 1254, 1254, 1254, 1254, color);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.translatable("craftaria_utils.wallpaper.reloading"), centerX, centerY + 34, color);
    }
}
