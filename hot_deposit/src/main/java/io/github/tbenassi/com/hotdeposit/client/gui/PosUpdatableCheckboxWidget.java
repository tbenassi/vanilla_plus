package io.github.tbenassi.com.hotdeposit.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import io.github.tbenassi.com.hotdeposit.client.HotDepositClient;
import io.github.tbenassi.com.hotdeposit.client.mixin.HandledScreenAccessor;
import io.github.tbenassi.com.hotdeposit.client.ClientState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;


@Environment(EnvType.CLIENT)
public class PosUpdatableCheckboxWidget extends CheckboxWidget {
    private static final Identifier TEXTURE_CUSTOM = new Identifier("hot-deposit", "textures/gui/checkbox_texture.png");
    private static final int TEXT_COLOR = 14737632;
    private final boolean showMessage;
    private final long containerPos;
    private final HandledScreen<?> parent;
    private final Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater;

    private PosUpdatableCheckboxWidget(int x, int y, int width, int height, Text text, Boolean checked, long containerPos, HandledScreen<?> parent, Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater) {
        super(x, y, width, height, text, checked, false);
        this.showMessage = false;
        this.containerPos = containerPos;
        this.parent = parent;
        this.posUpdater = posUpdater;
        this.setTooltip(Tooltip.of(text));
        Screens.getButtons(parent).add(this);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        posUpdater.ifPresent(updater -> setPos(updater.apply((HandledScreenAccessor) parent)));

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        RenderSystem.enableDepthTest();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();

        context.drawTexture(TEXTURE_CUSTOM, getX(), getY(), this.isHovered() ? 15.0F : 0.0F, this.isChecked() ? 15.0F : 0.0F, this.width, this.height, 64, 64);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.showMessage) {
            var textX = getX() - 58;
            var textY = getY() + (this.height - 8) / 2;
            context.drawTextWithShadow(textRenderer, this.getMessage(), textX, textY, TEXT_COLOR | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }

    // When checkbox is toggled
    @Override
    public void onPress() {
        super.onPress();
        this.storeCheckboxState(this.isChecked());
    }


    // Store checkbox state
    public void storeCheckboxState(boolean isChecked) {
        ClientState.toggleContainerChecked(containerPos, isChecked);
    }

    public void setPos(Vec2i pos) {
        setX(pos.x());
        setY(pos.y());
    }

    public static class Builder {
        private int x = 0;
        private int y = 0;
        private int width = 15;
        private int height = 15;
        @Nullable
        private Text text = ScreenTexts.EMPTY;
        private boolean checked = true;
        private long containerPos;
        private final HandledScreen<?> parent;
        private Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater = Optional.empty();

        public Builder(HandledScreen<?> parent) {
            this.parent = parent;
        }

        public Builder setPos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setText(Text text) {
            this.text = text;
            return this;
        }

        public Builder setChecked(Boolean checked) {
            this.checked = checked;
            return this;
        }

        public Builder setContainerPos(long containerPos) {
            this.containerPos = containerPos;
            return this;
        }

        public Builder setPosUpdater(Function<HandledScreenAccessor, Vec2i> posUpdater) {
            this.posUpdater = Optional.ofNullable(posUpdater);
            return this;
        }

        public void build() {
            new PosUpdatableCheckboxWidget(x, y, width, height, text, checked, containerPos, parent, posUpdater);
        }
    }
}

