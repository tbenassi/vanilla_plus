package io.github.tbenassi.com.hotdeposit.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import io.github.tbenassi.com.hotdeposit.client.mixin.HandledScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class CustomCheckboxWidget extends PressableWidget {
    private static final Identifier TEXTURE_CUSTOM = Identifier.of("hot-deposit", "textures/gui/checkbox_texture.png");
    private static final int TEXT_COLOR = 14737632;
    private final boolean showMessage;
    private final HandledScreen<?> parent;
    private final Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater;
    private boolean checked;
    private final Callback callback;

    CustomCheckboxWidget(int x, int y, int width, int height,
                         Text message, boolean checked, Callback callback, Boolean showMessage, HandledScreen<?> parent,
                         Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater
    ) {
        super(x, y, 0, 0, message);
        this.width = width;
        this.height = height;
        this.checked = checked;
        this.callback = callback;
        this.showMessage = showMessage;
        this.parent = parent;
        this.posUpdater = posUpdater;
        this.setTooltip(Tooltip.of(message));
        Screens.getButtons(parent).add(this);
    }

    static int calculateWidth(Text text, TextRenderer textRenderer) {
        return CustomCheckboxWidget.getCheckboxSize(textRenderer) + 4 + textRenderer.getWidth(text);
    }

    static int getCheckboxSize(TextRenderer textRenderer) {
        return textRenderer.fontHeight + 8;
    }


    @Override
    public void onPress() {
        this.checked = !this.checked;
        this.callback.onValueChange(this, this.checked);
    }

    public boolean isChecked() {
        return this.checked;
    }

    private void setPos(Vec2i pos) {
        setX(pos.x());
        setY(pos.y());
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.checkbox.usage.focused"));
            } else {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        posUpdater.ifPresent(updater -> setPos(updater.apply((HandledScreenAccessor) parent)));
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        RenderSystem.enableDepthTest();
        context.getMatrices().push();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Texture dimensions (assuming 64x64 texture atlas)
        int textureWidth = 64;
        int textureHeight = 64;

        // Calculate the u/v coordinates based on the state
        float u = this.isHovered() ? 15.0F : 0.0F;
        float v = this.isChecked() ? 15.0F : 0.0F;

        context.drawTexture(
                RenderLayer::getGuiTextured,
                TEXTURE_CUSTOM,
                getX(), getY(),
                u, v,
                this.width, this.height,
                textureWidth, textureHeight);

        context.getMatrices().pop();

        if (this.showMessage) {
            var textX = getX() - 58;
            var textY = getY() + (this.height - 8) / 2;
            context.drawTextWithShadow(textRenderer, this.getMessage(), textX, textY, TEXT_COLOR | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public interface Callback {
        CustomCheckboxWidget.Callback EMPTY = (checkbox, checked) -> {};

        void onValueChange(CustomCheckboxWidget var1, boolean var2);
    }

    public static CustomCheckboxWidget.Builder builder(HandledScreen<?> parent, Text text) {
        return new CustomCheckboxWidget.Builder(parent, text);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Text message;
        private boolean showMessage = false;
        private int width = 15;
        private int height = 15;
        private int x = 0;
        private int y = 0;
        private CustomCheckboxWidget.Callback callback = CustomCheckboxWidget.Callback.EMPTY;
        private boolean checked = false;
        private final HandledScreen<?> parent;
        private Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater = Optional.empty();

        Builder(HandledScreen<?> parent, Text message) {
            this.parent = parent;
            this.message = message;
            if (!this.message.getString().isEmpty())
                this.showMessage = true;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder callback(CustomCheckboxWidget.Callback callback) {
            this.callback = callback;
            return this;
        }

        public Builder checked(boolean checked) {
            this.checked = checked;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder posUpdater(Function<HandledScreenAccessor, Vec2i> posUpdater) {
            this.posUpdater = Optional.ofNullable(posUpdater);
            return this;
        }

        public void build() {
            CustomCheckboxWidget.Callback callback = this.callback != null ? this.callback : (checkbox, checked) -> {
                this.callback.onValueChange(checkbox, checked);
            };
            new CustomCheckboxWidget(this.x, this.y, this.width, this.height, this.message, this.checked, callback,
                    this.showMessage, this.parent, this.posUpdater);
        }
    }
}

