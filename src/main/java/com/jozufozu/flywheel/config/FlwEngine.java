package com.jozufozu.flywheel.config;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.OptifineHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public enum FlwEngine {
	BATCHING(new TextComponent("Batching").withStyle(ChatFormatting.BLUE)),
	GL33(new TextComponent("GL 3.3 Instanced Arrays").withStyle(ChatFormatting.GREEN)),

	;

	private final Component name;

	FlwEngine(Component name) {
		this.name = name;
	}

	@Nullable
	public static FlwEngine decode(FriendlyByteBuf buffer) {
		byte b = buffer.readByte();

		if (b == -1) return null;

		return values()[b];
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeByte(this.ordinal());
	}

	public void switchTo() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

//		if (state == BooleanDirective.DISPLAY) {
//			Component text = new TextComponent("Flywheel renderer is currently: ").append(boolToText(FlwConfig.get().enabled()));
//			player.displayClientMessage(text, false);
//			return;
//		}

		FlwConfig.get().client.engine.set(this);

		Component text = new TextComponent("Using ").withStyle(ChatFormatting.WHITE).append(name);

		player.displayClientMessage(text, false);
		Backend.reloadWorldRenderers();
	}
}
