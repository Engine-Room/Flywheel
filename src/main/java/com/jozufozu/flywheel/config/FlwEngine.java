package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.ModList;

public enum FlwEngine {
	OFF("off", "Off"),
	BATCHING("batching", "Parallel Batching"),
	INSTANCING("instancing", "GL33 Instanced Arrays"),
	;

	private static final Map<String, FlwEngine> lookup;

	static {
		lookup = new HashMap<>();
		for (FlwEngine value : values()) {
			lookup.put(value.shortName, value);
		}
	}

	private final String shortName;
	private final String properName;

	FlwEngine(String shortName, String properName) {
		this.shortName = shortName;
		this.properName = properName;
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

			Component message = getMessage(type);

			player.displayClientMessage(message, false);
			Backend.reloadWorldRenderers();
		} else {
			player.displayClientMessage(getMessage(FlwConfig.get().getEngine()), false);
		}
	}

	private static Component getMessage(@NotNull FlwEngine type) {
		return switch (type) {
			case OFF -> new TextComponent("Disabled Flywheel").withStyle(ChatFormatting.RED);
			case INSTANCING -> new TextComponent("Using Instancing Engine").withStyle(ChatFormatting.GREEN);
			case BATCHING -> {
				MutableComponent msg = new TextComponent("Using Batching Engine").withStyle(ChatFormatting.GREEN);

				if (ModList.get()
						.isLoaded("create")) {
					// FIXME: batching engine contraption lighting issues
					msg.append(new TextComponent("\nWARNING: May cause issues with Create Contraptions").withStyle(ChatFormatting.RED));
				}

				yield msg;
			}
		};
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
