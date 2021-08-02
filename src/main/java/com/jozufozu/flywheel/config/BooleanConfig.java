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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum BooleanConfig {
	ENGINE(() -> BooleanConfig::enabled),
	NORMAL_OVERLAY(() -> BooleanConfig::normalOverlay),
	CHUNK_CACHING(() -> BooleanConfig::chunkCaching),
	;

	final Supplier<Consumer<BooleanDirective>> receiver;

	BooleanConfig(Supplier<Consumer<BooleanDirective>> receiver) {
		this.receiver = receiver;
	}

	public SConfigureBooleanPacket packet(BooleanDirective directive) {
		return new SConfigureBooleanPacket(this, directive);
	}

	@OnlyIn(Dist.CLIENT)
	private static void enabled(BooleanDirective state) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			ITextComponent text = new StringTextComponent("Flywheel renderer is currently: ").append(boolToText(FlwConfig.get().client.enabled.get()));
			player.displayClientMessage(text, false);
			return;
		}

		boolean enabled = state.get();
		boolean cannotUse = OptifineHandler.usingShaders() && enabled;

		FlwConfig.get().client.enabled.set(enabled);

		ITextComponent text = boolToText(FlwConfig.get().client.enabled.get()).append(new StringTextComponent(" Flywheel renderer").withStyle(TextFormatting.WHITE));
		ITextComponent error = new StringTextComponent("Flywheel renderer does not support Optifine Shaders").withStyle(TextFormatting.RED);

		player.displayClientMessage(cannotUse ? error : text, false);
		Backend.reloadWorldRenderers();
	}

	@OnlyIn(Dist.CLIENT)
	private static void normalOverlay(BooleanDirective state) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			ITextComponent text = new StringTextComponent("Normal debug mode is currently: ").append(boolToText(FlwConfig.get().client.debugNormals.get()));
			player.displayClientMessage(text, false);
			return;
		}

		FlwConfig.get().client.debugNormals.set(state.get());

		ITextComponent text = boolToText(FlwConfig.get().client.debugNormals.get()).append(new StringTextComponent(" normal debug mode").withStyle(TextFormatting.WHITE));

		player.displayClientMessage(text, false);
	}

	@OnlyIn(Dist.CLIENT)
	private static void chunkCaching(BooleanDirective state) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			ITextComponent text = new StringTextComponent("Chunk caching is currently: ").append(boolToText(FlwConfig.get().client.debugNormals.get()));
			player.displayClientMessage(text, false);
			return;
		}

		FlwConfig.get().client.chunkCaching.set(state.get());

		ITextComponent text = boolToText(FlwConfig.get().client.chunkCaching.get()).append(new StringTextComponent(" chunk caching").withStyle(TextFormatting.WHITE));

		player.displayClientMessage(text, false);
		Backend.reloadWorldRenderers();
	}

	private static IFormattableTextComponent boolToText(boolean b) {
		return b ? new StringTextComponent("enabled").withStyle(TextFormatting.DARK_GREEN) : new StringTextComponent("disabled").withStyle(TextFormatting.RED);
	}
}
