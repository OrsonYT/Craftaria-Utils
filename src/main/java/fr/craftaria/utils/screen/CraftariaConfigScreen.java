package fr.craftaria.utils.screen;

import fr.craftaria.utils.CraftariaUtilsClient;
import fr.craftaria.utils.config.ModConfig;
import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class CraftariaConfigScreen extends CraftariaScreen {
    private static final Identifier LOGO = Identifier.of("craftaria_utils", "icon-transparent.png");
    private enum Page { REPAIR, LOGIN, COOKING, KEYS }
    private final Screen parent;
    private Page page = Page.REPAIR;
    private KeyBinding capturing;
    private TextFieldWidget passwordField;
    private int left, top, contentX, contentWidth, bodyY;

    public CraftariaConfigScreen(Screen parent) { super(Text.translatable("craftaria_utils.title")); this.parent = parent; }

    @Override protected void init() {
        capturing = null;
        passwordField = null;
        int totalWidth = Math.min(620, width-28);
        left = (width-totalWidth)/2;
        top = Math.max(12, (height-280)/2);
        int sidebarWidth = width < 400 ? 92 : 128;
        contentX = left+sidebarWidth+20;
        contentWidth = totalWidth-sidebarWidth-20;
        bodyY = top+44;

        Page[] pages = Page.values();
        int sidebarSpacing = Math.min(27, Math.max(19, (height-43-(bodyY+8)-22)/(pages.length+1)));
        for (int i=0; i<pages.length; i++) {
            Page tab = pages[i];
            addDrawableChild(new MenuTextButton(left+8, bodyY+8+i*sidebarSpacing, sidebarWidth-10, 22,
                    Text.translatable(pageKey(tab)), 1f, () -> { savePassword(); page=tab; clearAndInit(); }));
        }
        addDrawableChild(new MenuTextButton(left+8, bodyY+8+pages.length*sidebarSpacing, sidebarWidth-10, 22,
                Text.translatable("craftaria_utils.settings.wallpaper"), 1f, () -> {
            savePassword(); client.setScreen(new WallpaperGalleryScreen(this));
        }));
        addDrawableChild(new MenuTextButton(left+8, bodyY+8+(pages.length+1)*sidebarSpacing, sidebarWidth-10, 22,
                Text.translatable("craftaria_utils.quick_homes.tab"), 1f, () -> {
            savePassword(); client.setScreen(new QuickHomesScreen(this));
        }));
        addDrawableChild(new MenuTextButton(left+8, height-27, 100, 20, Text.translatable("gui.back"), 1f, this::close));
        addDrawableChild(new MenuTextButton(width-115, height-27, 104, 20,
                Text.translatable("craftaria_utils.settings.controls"), .85f, () -> {
                    savePassword(); client.setScreen(new ControlsOptionsScreen(this, client.options));
                }));

        int y = bodyY+45;
        switch (page) {
            case REPAIR -> {
                addDrawableChild(new SettingsButton(contentX, y, contentWidth, () -> Text.translatable("craftaria_utils.repair",
                        Text.translatable(ModConfig.get().autoRepair ? "options.on" : "options.off")), () -> {
                    ModConfig.get().autoRepair = !ModConfig.get().autoRepair;
                    ModConfig.save();
                }));
                addDrawableChild(new RepairThresholdSlider(contentX, y+38, contentWidth));
            }
            case LOGIN -> {
                passwordField = new TextFieldWidget(textRenderer, contentX+1, y+13, contentWidth-2, 22,
                        Text.translatable("craftaria_utils.login.password"));
                passwordField.setMaxLength(128);
                passwordField.setText(ModConfig.get().password);
                passwordField.setEditableColor(0xFFF0E9DA);
                passwordField.setDrawsBackground(false);
                passwordField.addFormatter((value, cursor) -> Text.literal("•".repeat(value.length())).asOrderedText());
                passwordField.setChangedListener(value -> { ModConfig.get().password=value; ModConfig.save(); });
                addDrawableChild(passwordField);
                addBindingRow(CraftariaUtilsClient.loginKey, "craftaria_utils.settings.login_key", y+52);
            }
            case COOKING -> {
                addDrawableChild(new CookingDelaySlider(contentX, y, contentWidth));
                addBindingRow(CraftariaUtilsClient.cookingKey, "craftaria_utils.settings.cooking_key", y+35);
                addDrawableChild(new SettingsButton(contentX, y+70, contentWidth,
                        () -> Text.translatable("craftaria_utils.open_cooking"), () -> {
                    if (client.player != null) { savePassword(); client.setScreen(new CookingScreen(this)); }
                }));
            }
            case KEYS -> {
                addBindingRow(CraftariaUtilsClient.fourKey, "craftaria_utils.settings.four_key", y);
                addBindingRow(CraftariaUtilsClient.loginKey, "craftaria_utils.settings.login_key", y+29);
                addBindingRow(CraftariaUtilsClient.cookingKey, "craftaria_utils.settings.cooking_key", y+58);
            }
        }
    }

    private static String pageKey(Page page) { return "craftaria_utils.settings."+page.name().toLowerCase(java.util.Locale.ROOT); }

    private void addBindingRow(KeyBinding binding, String label, int y) {
        int keyWidth = Math.min(115, contentWidth/2);
        addDrawableChild(new SettingsButton(contentX+contentWidth-keyWidth, y, keyWidth, () -> {
            if (capturing == binding) return Text.translatable("craftaria_utils.settings.press_key");
            Text key = binding.getBoundKeyLocalizedText();
            return hasConflict(binding) ? Text.translatable("craftaria_utils.settings.conflict_key", key) : key;
        }, () -> { capturing=binding; setFocused(null); }));
    }

    private boolean hasConflict(KeyBinding binding) {
        if (binding.isUnbound()) return false;
        for (KeyBinding other : client.options.allKeys) if (other != binding && binding.equals(other)) return true;
        return false;
    }

    private void bind(InputUtil.Key key) {
        capturing.setBoundKey(key);
        KeyBinding.updateKeysByCode();
        KeyBinding.unpressAll();
        client.options.write();
        capturing = null;
    }

    @Override public boolean keyPressed(KeyInput input) {
        if (capturing != null) {
            bind(input.key()==GLFW.GLFW_KEY_ESCAPE ? InputUtil.UNKNOWN_KEY : InputUtil.fromKeyCode(input));
            return true;
        }
        return super.keyPressed(input);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        if (capturing != null) {
            bind(InputUtil.Type.MOUSE.createFromCode(click.button()));
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private void savePassword() {
        if (passwordField != null) ModConfig.get().password=passwordField.getText();
        ModConfig.save();
    }
    @Override public void close() { savePassword(); client.setScreen(parent); }
    @Override public void removed() { savePassword(); }

    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        WallpaperManager.draw(context, width, height);
        context.fill(0, 0, width, height, 0xAB0A1017);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO, left, top-1, 0, 0, 36, 36, 1254, 1254, 1254, 1254);
        MenuTextButton.drawLabel(context, title, left+46, top+3, 1.5f, false, true);
        context.drawTextWithShadow(textRenderer, Text.translatable("craftaria_utils.settings.subtitle"), left+46, top+23, 0xFFB8B4AA);
        context.fill(contentX-11, bodyY, contentX-10, height-43, 0x606E6554);
        context.drawTextWithShadow(textRenderer, Text.translatable(pageKey(page)), contentX, bodyY+3, 0xFFFFD383);
        context.drawWrappedTextWithShadow(textRenderer, Text.translatable(pageKey(page)+".description"),
                contentX, bodyY+20, contentWidth, 0xFFC5C1B8);
        int y=bodyY+45;
        if (page==Page.LOGIN) {
            context.drawTextWithShadow(textRenderer, Text.translatable("craftaria_utils.login.password"), contentX, y, 0xFFF0E9DA);
            context.fill(contentX, y+12, contentX+contentWidth, y+36, 0x50101720);
            context.fill(contentX, y+35, contentX+contentWidth, y+36, 0x806E6554);
            bindingLabel(context, "craftaria_utils.settings.login_key", y+52);
            if (y+103 < height-46) context.drawWrappedTextWithShadow(textRenderer, Text.translatable("craftaria_utils.password_storage"), contentX, y+83, contentWidth, 0xFF99968F);
        } else if (page==Page.COOKING) bindingLabel(context, "craftaria_utils.settings.cooking_key", y+35);
        else if (page==Page.KEYS) {
            bindingLabel(context, "craftaria_utils.settings.four_key", y);
            bindingLabel(context, "craftaria_utils.settings.login_key", y+29);
            bindingLabel(context, "craftaria_utils.settings.cooking_key", y+58);
        } else if (page==Page.REPAIR) {
            context.drawWrappedTextWithShadow(textRenderer, Text.translatable("craftaria_utils.settings.repair.note"), contentX, y+70, contentWidth, 0xFF99968F);
        }
        if (capturing != null) context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("craftaria_utils.settings.capture_hint"), width/2, height-41, 0xFFFFD383);
        else context.drawCenteredTextWithShadow(textRenderer, Text.translatable("craftaria_utils.settings.saved"), width/2, height-41, 0xFF99968F);
        super.render(context, mouseX, mouseY, delta);
    }

    private void bindingLabel(DrawContext context, String key, int y) {
        int maxWidth=contentWidth-Math.min(115, contentWidth/2)-7;
        context.drawTextWithShadow(textRenderer, Text.literal(textRenderer.trimToWidth(Text.translatable(key).getString(), maxWidth)), contentX, y+7, 0xFFF0E9DA);
    }
}
