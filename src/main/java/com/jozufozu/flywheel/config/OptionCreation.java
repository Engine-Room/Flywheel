package com.jozufozu.flywheel.config;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.OptifineHandler;
import com.jozufozu.flywheel.config.Option.BooleanOption;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public final class OptionCreation {
	public static BooleanOption enabled() {
		return new BooleanOption("enabled", true,
			source -> {
				ITextComponent text = new StringTextComponent("Flywheel Renderer is currently: ").append(boolToText(FlwConfig.get().enabled.get()));
				source.sendFeedback(text);
			},
			(source, value) -> {
				if (OptifineHandler.usingShaders() && value) {
					ITextComponent error = new StringTextComponent("Flywheel Renderer does not support Optifine Shaders").formatted(TextFormatting.RED);
					source.sendFeedback(error);
				} else {
					ITextComponent text = boolToText(value).append(new StringTextComponent(" Flywheel Renderer").formatted(TextFormatting.WHITE));
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
				ITextComponent text = new StringTextComponent("Normal overlay is currently: ").append(boolToText(FlwConfig.get().normalOverlay.get()));
				source.sendFeedback(text);
			},
			(source, value) -> {
				ITextComponent text = boolToText(value).append(new StringTextComponent(" Normal Overlay").formatted(TextFormatting.WHITE));
				source.sendFeedback(text);
				FlwConfig.save();
			}
		);
	}

	private static IFormattableTextComponent boolToText(boolean b) {
		return b ? new StringTextComponent("enabled").formatted(TextFormatting.DARK_GREEN) : new StringTextComponent("disabled").formatted(TextFormatting.RED);
	}
}
