package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public enum FlwEngine {
	OFF("off", "Off", new TextComponent("Disabled Flywheel").withStyle(ChatFormatting.RED)),
	BATCHING("batching", "Parallel Batching", new TextComponent("Using Batching Engine").withStyle(ChatFormatting.GREEN)),
	INSTANCING("instancing", "GL33 Instanced Arrays", new TextComponent("Using Instancing Engine").withStyle(ChatFormatting.GREEN)),
	;

	private static final Map<String, FlwEngine> lookup;

	static {
		lookup = new HashMap<>();
		for (FlwEngine value : values()) {
			lookup.put(value.shortName, value);
		}
	}

	private final Component message;
	private final String shortName;
	private final String properName;

	FlwEngine(String shortName, String properName, Component message) {
		this.shortName = shortName;
		this.properName = properName;
		this.message = message;
	}

	public String getProperName() {
		return properName;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeByte(this.ordinal());
	}

	public static void handle(@Nullable FlwEngine type) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		if (type != null) {
			FlwConfig.get().client.engine.set(type);

			player.displayClientMessage(type.message, false);
			Backend.reloadWorldRenderers();
		} else {
			player.displayClientMessage(FlwConfig.get().getEngine().message, false);
		}
	}

	@Nullable
	public static FlwEngine decode(FriendlyByteBuf buffer) {
		byte b = buffer.readByte();

		if (b == -1) return null;

		return values()[b];
	}

	@Nullable
	public static FlwEngine byName(String name) {
		return lookup.get(name);
	}

	public static Collection<String> validNames() {
		return lookup.keySet();
	}
}
