package fr.craftaria.utils.screen;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class CraftariaTitleScreen extends Screen {
    public static final String SERVER = "play.craftaria.fr";
    private static final Identifier BACKGROUND = Identifier.of("craftaria_utils", "fondbase.png");
    private static final Identifier LOGO = Identifier.of("craftaria_utils", "icon-transparent.png");
    private boolean connecting;
    private final ServerStatusPanel serverStatus = new ServerStatusPanel();
    private MenuTextButton connectButton;

    public CraftariaTitleScreen() {
        super(Text.literal("Craftaria Utils"));
        MenuNavigation.selectCustom();
    }
    public float layoutScale() { return Math.min(width / 960f, height / 540f); }
    public int left() { return Math.round(64 * layoutScale()); }

    @Override protected void init() {
        connecting = false;
        serverStatus.open();
        float s = layoutScale();
        int left = left();
        connectButton = addDrawableChild(new MenuTextButton(left, Math.round(185*s), Math.round(570*s), Math.round(48*s),
                Text.translatable("craftaria_utils.menu.connect"), 3.2f*s, this::connect));
        addDrawableChild(new MenuTextButton(left, Math.round(285*s), Math.round(500*s), Math.round(32*s),
                Text.translatable("craftaria_utils.menu.vanilla"), 2.1f*s, () -> client.setScreen(new VanillaTitleScreen())));
        addDrawableChild(new MenuTextButton(left, Math.round(333*s), Math.round(320*s), Math.round(32*s),
                Text.translatable("menu.options"), 2.1f*s, () -> client.setScreen(new OptionsScreen(this, client.options))));
        addDrawableChild(new MenuTextButton(left, Math.round(381*s), Math.round(320*s), Math.round(32*s),
                Text.translatable("menu.quit"), 2.1f*s, () -> client.scheduleStop()));
    }

    private void connect() {
        if (connecting) return;
        connecting = true;
        connectButton.setMessage(Text.translatable("craftaria_utils.menu.connecting"));
        connectButton.active = false;
        ConnectScreen.connect(this, client, ServerAddress.parse(SERVER),
                new ServerInfo("Craftaria", SERVER, ServerInfo.ServerType.OTHER), false, null);
    }

    public void renderScene(DrawContext context, int width, int height) {
        float cover = Math.max(width / 2560f, height / 1600f);
        int w = (int)Math.ceil(2560*cover), h = (int)Math.ceil(1600*cover);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, (width-w)/2, (height-h)/2,
                0, 0, w, h, 2560, 1600, 2560, 1600);
        for (int x = 0; x < width * .65f; x += 2) {
            int alpha = Math.round(72 * (1 - x / (width * .65f)));
            context.fill(x, 0, x+2, height, alpha << 24);
        }
        float s = Math.min(width/960f, height/540f);
        int size = Math.round(74*s), left = Math.round(36*s), top = Math.round(26*s);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO, left, top, 0, 0, size, size, 1254, 1254, 1254, 1254);
        serverStatus.render(context, width, height);
        String[] credits = {
                "Craftaria Utils by OrsonBlockTV",
                "Minecraft 1.21.11 — Thanks to Fabric for the loader",
                "Copyright Mojang AB. Do not distribute!"
        };
        float creditScale = Math.clamp(s, 0.5f, 0.7f);
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(creditScale, creditScale);
        for (int i = 0; i < credits.length; i++) {
            int creditX = (int)((width - 6) / creditScale) - textRenderer.getWidth(credits[i]);
            int creditY = (int)((height - 6) / creditScale) - (credits.length - i) * 10;
            context.drawTextWithShadow(textRenderer, Text.literal(credits[i]), creditX, creditY, 0xFFCDC7BA);
        }
        context.getMatrices().popMatrix();
    }

    public void renderConnecting(DrawContext context, int width, int height, Text status) {
        renderScene(context, width, height);
        float s = Math.min(width/960f, height/540f);
        MenuTextButton.drawLabel(context, Text.translatable("craftaria_utils.menu.connecting"), Math.round(64*s), Math.round(185*s), 3.2f*s, false, false);
        context.drawTextWithShadow(textRenderer, status, Math.round(64*s), Math.round(246*s), 0xFFE2D9C4);
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderScene(context, width, height);
        super.render(context, mouseX, mouseY, delta);
    }
    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { }
    @Override public void tick() { serverStatus.tick(); }
    @Override public void removed() { serverStatus.close(); }
    @Override public boolean shouldCloseOnEsc() { return false; }

    /** Explicit escape hatch: its subclass identity prevents automatic menu replacement. */
    public static final class VanillaTitleScreen extends TitleScreen {
        public VanillaTitleScreen() { MenuNavigation.selectVanilla(); }
    }
}
