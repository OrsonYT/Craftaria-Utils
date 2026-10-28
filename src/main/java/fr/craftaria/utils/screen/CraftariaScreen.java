package fr.craftaria.utils.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Shared screen base that avoids requesting the global menu blur more than once per frame. */
public abstract class CraftariaScreen extends Screen {
    protected CraftariaScreen(Text title) { super(title); }

    @Override
    protected void applyBlur(float delta) {
        // Vanilla may already have blurred this frame while replacing the pause screen.
    }
}
