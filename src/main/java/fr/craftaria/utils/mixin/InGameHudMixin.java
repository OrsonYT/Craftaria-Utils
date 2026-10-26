package fr.craftaria.utils.mixin;

import fr.craftaria.utils.cooking.CookingQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void craftariaUtils$renderCookingProgress(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!CookingQueue.isActive() || client.player == null || client.currentScreen != null) return;

        int total = CookingQueue.getTotalCount();
        int current = Math.min(CookingQueue.getCompletedCount() + 1, total);
        int width = 164;
        int height = 38;
        int x = (context.getScaledWindowWidth() - width) / 2;
        int y = context.getScaledWindowHeight() / 2 + 26;

        context.fill(x, y, x + width, y + height, 0xCC101015);
        context.fill(x, y, x + width, y + 1, 0xFFFFB94A);
        context.fill(x, y + height - 1, x + width, y + height, 0xFFFFB94A);
        context.fill(x, y + 1, x + 1, y + height - 1, 0xFFFFB94A);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, 0xFFFFB94A);

        int bob = (client.player.age / 5) % 2;
        context.drawItem(Items.FURNACE.getDefaultStack(), x + 7, y + 7 + bob);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("craftaria_utils.cooking.progress", current, total),
                x + 29, y + 6, 0xFFFFFFFF);

        int barX = x + 29;
        int barY = y + 23;
        int barWidth = width - 38;
        context.fill(barX, barY, barX + barWidth, barY + 5, 0xFF45454B);
        int progressWidth = Math.round(barWidth * CookingQueue.getProgress());
        if (progressWidth > 0) context.fill(barX, barY, barX + progressWidth, barY + 5, 0xFFFFB94A);
    }
}
