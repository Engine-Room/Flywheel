package com.jozufozu.flywheel.config;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.OptifineHandler;
import com.jozufozu.flywheel.config.Option.BooleanOption;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public final class OptionCreation {
	public static BooleanOption enabled() {
		return new BooleanOption("enabled", true,
			source -> {
				Component text = new TextComponent("Flywheel Renderer is currently: ").append(boolToText(FlwConfig.get().enabled.get()));
				source.sendFeedback(text);
			},
			(source, value) -> {
				if (OptifineHandler.usingShaders() && value) {
					Component error = new TextComponent("Flywheel Renderer does not support Optifine Shaders").withStyle(ChatFormatting.RED);
					source.sendFeedback(error);
				} else {
					Component text = boolToText(value).append(new TextComponent(" Flywheel Renderer").withStyle(ChatFormatting.WHITE));
					source.sendFeedback(text);
				}
				Backend.reloadWorldRenderers();
				FlwConfig.save();
			}
		);
	}

	public static BooleanOption normalOverlay() {
		return new BooleanOption("normalOverlay", false,
			source -> {
				Component text = new TextComponent("Normal overlay is currently: ").append(boolToText(FlwConfig.get().normalOverlay.get()));
				source.sendFeedback(text);
			},
			(source, value) -> {
				Component text = boolToText(value).append(new TextComponent(" Normal Overlay").withStyle(ChatFormatting.WHITE));
				source.sendFeedback(text);
				FlwConfig.save();
			}
		);
	}

	private static MutableComponent boolToText(boolean b) {
		return b ? new TextComponent("enabled").withStyle(ChatFormatting.DARK_GREEN) : new TextComponent("disabled").withStyle(ChatFormatting.RED);
	}
}
