package com.jozufozu.flywheel.config;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public enum BooleanConfig {
	NORMAL_OVERLAY(BooleanConfig::normalOverlay),
	LIMIT_UPDATES(BooleanConfig::limitUpdates);

	final Consumer<BooleanDirective> receiver;

	BooleanConfig(Consumer<BooleanDirective> receiver) {
		this.receiver = receiver;
	}

	private static void limitUpdates(BooleanDirective booleanDirective) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || booleanDirective == null) return;

		if (booleanDirective == BooleanDirective.DISPLAY) {
			Component text = new TextComponent("Update limiting is currently: ").append(boolToText(FlwConfig.get().limitUpdates()));
			player.displayClientMessage(text, false);
			return;
		}

		FlwConfig.get().client.limitUpdates.set(booleanDirective.get());

		Component text = boolToText(FlwConfig.get().limitUpdates()).append(new TextComponent(" update limiting.").withStyle(ChatFormatting.WHITE));

		player.displayClientMessage(text, false);

		Backend.reloadWorldRenderers();
	}

	private static void normalOverlay(BooleanDirective state) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			Component text = new TextComponent("Normal debug mode is currently: ").append(boolToText(FlwConfig.get().debugNormals()));
			player.displayClientMessage(text, false);
			return;
		}

		FlwConfig.get().client.debugNormals.set(state.get());

		Component text = boolToText(FlwConfig.get().debugNormals()).append(new TextComponent(" normal debug mode").withStyle(ChatFormatting.WHITE));

		player.displayClientMessage(text, false);
	}

	public static MutableComponent boolToText(boolean b) {
		return b ? new TextComponent("enabled").withStyle(ChatFormatting.DARK_GREEN) : new TextComponent("disabled").withStyle(ChatFormatting.RED);
	}
}
