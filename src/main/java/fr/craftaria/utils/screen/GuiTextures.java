package fr.craftaria.utils.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** Scaled GUI textures on the Minecraft 1.21.1 renderer. */
public final class GuiTextures {
    private GuiTextures() { }
    public static void draw(DrawContext c, Identifier id, int x, int y, float u, float v,
            int width, int height, int textureWidth, int textureHeight) {
        draw(c,id,x,y,u,v,width,height,width,height,textureWidth,textureHeight);
    }
    public static void draw(DrawContext c, Identifier id, int x, int y, float u, float v,
            int width, int height, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        c.draw();
        RenderSystem.enableBlend();
        c.drawTexture(id,x,y,width,height,u,v,regionWidth,regionHeight,textureWidth,textureHeight);
    }
    public static void draw(DrawContext c, Identifier id, int x, int y, float u, float v,
            int width, int height, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color) {
        c.draw();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,(color>>>24)/255f);
        draw(c,id,x,y,u,v,width,height,regionWidth,regionHeight,textureWidth,textureHeight);
        c.draw();
        RenderSystem.setShaderColor(1,1,1,1);
    }
}
