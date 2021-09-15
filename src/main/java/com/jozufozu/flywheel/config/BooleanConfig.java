package com.jozufozu.flywheel.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.OptifineHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
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
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			Component text = new TextComponent("Flywheel renderer is currently: ").append(boolToText(FlwConfig.get().client.enabled.get()));
			player.displayClientMessage(text, false);
			return;
		}

		boolean enabled = state.get();
		boolean cannotUse = OptifineHandler.usingShaders() && enabled;

		FlwConfig.get().client.enabled.set(enabled);

		Component text = boolToText(FlwConfig.get().client.enabled.get()).append(new TextComponent(" Flywheel renderer").withStyle(ChatFormatting.WHITE));
		Component error = new TextComponent("Flywheel renderer does not support Optifine Shaders").withStyle(ChatFormatting.RED);

		player.displayClientMessage(cannotUse ? error : text, false);
		Backend.reloadWorldRenderers();
	}

	@OnlyIn(Dist.CLIENT)
	private static void normalOverlay(BooleanDirective state) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			Component text = new TextComponent("Normal debug mode is currently: ").append(boolToText(FlwConfig.get().client.debugNormals.get()));
			player.displayClientMessage(text, false);
			return;
		}

		FlwConfig.get().client.debugNormals.set(state.get());

		Component text = boolToText(FlwConfig.get().client.debugNormals.get()).append(new TextComponent(" normal debug mode").withStyle(ChatFormatting.WHITE));

		player.displayClientMessage(text, false);
	}

	@OnlyIn(Dist.CLIENT)
	private static void chunkCaching(BooleanDirective state) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			Component text = new TextComponent("Chunk caching is currently: ").append(boolToText(FlwConfig.get().client.chunkCaching.get()));
			player.displayClientMessage(text, false);
			return;
		}

		FlwConfig.get().client.chunkCaching.set(state.get());

		Component text = boolToText(FlwConfig.get().client.chunkCaching.get()).append(new TextComponent(" chunk caching").withStyle(ChatFormatting.WHITE));

		player.displayClientMessage(text, false);
		Backend.reloadWorldRenderers();
	}

	private static MutableComponent boolToText(boolean b) {
		return b ? new TextComponent("enabled").withStyle(ChatFormatting.DARK_GREEN) : new TextComponent("disabled").withStyle(ChatFormatting.RED);
	}
}
