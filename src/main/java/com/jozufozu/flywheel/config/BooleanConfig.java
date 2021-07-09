package com.jozufozu.flywheel.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.OptifineHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public enum BooleanConfig {
	ENGINE(() -> BooleanConfig::enabled),
	NORMAL_OVERLAY(() -> BooleanConfig::normalOverlay),
	;

	final Supplier<Consumer<BooleanDirective>> receiver;

	BooleanConfig(Supplier<Consumer<BooleanDirective>> receiver) {
		this.receiver = receiver;
	}

	public SConfigureBooleanPacket packet(BooleanDirective directive) {
		return new SConfigureBooleanPacket(this, directive);
	}

	@Environment(EnvType.CLIENT)
	private static void enabled(BooleanDirective state) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			ITextComponent text = new StringTextComponent("Flywheel Renderer is currently: ").append(boolToText(FlwConfig.get().client.enabled.get()));
			player.sendStatusMessage(text, false);
			return;
		}

		boolean enabled = state.get();
		boolean cannotUseER = OptifineHandler.usingShaders() && enabled;

		FlwConfig.get().client.enabled.set(enabled);

		ITextComponent text = boolToText(FlwConfig.get().client.enabled.get()).append(new StringTextComponent(" Flywheel Renderer").formatted(TextFormatting.WHITE));
		ITextComponent error = new StringTextComponent("Flywheel Renderer does not support Optifine Shaders").formatted(TextFormatting.RED);

		player.sendStatusMessage(cannotUseER ? error : text, false);
		Backend.reloadWorldRenderers();
	}

	@Environment(EnvType.CLIENT)
	private static void normalOverlay(BooleanDirective state) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			ITextComponent text = new StringTextComponent("Normal overlay is currently: ").append(boolToText(FlwConfig.get().client.normalDebug.get()));
			player.sendStatusMessage(text, false);
			return;
		}

		FlwConfig.get().client.normalDebug.set(state.get());

		ITextComponent text = boolToText(FlwConfig.get().client.normalDebug.get()).append(new StringTextComponent(" Normal Overlay").formatted(TextFormatting.WHITE));

		player.sendStatusMessage(text, false);
	}

	private static IFormattableTextComponent boolToText(boolean b) {
		return b ? new StringTextComponent("enabled").formatted(TextFormatting.DARK_GREEN) : new StringTextComponent("disabled").formatted(TextFormatting.RED);
	}
}
