package com.jozufozu.flywheel.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum BooleanConfig {
	NORMAL_OVERLAY(() -> BooleanConfig::normalOverlay),
	;

	final Supplier<Consumer<BooleanDirective>> receiver;

	BooleanConfig(Supplier<Consumer<BooleanDirective>> receiver) {
		this.receiver = receiver;
	}

	public SConfigureBooleanPacket packet(BooleanDirective directive) {
		return new SConfigureBooleanPacket(this, directive);
	}

	/**
	 * Encode a variant of BooleanConfig. Symmetrical function to {@link #decode}
	 */
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeByte(this.ordinal());
	}

	/**
	 * Safely decode a variant of BooleanConfig. Symmetrical function to {@link #encode}
	 */
	public static BooleanConfig decode(FriendlyByteBuf buffer) {
		byte t = buffer.readByte();
		BooleanConfig[] values = values();
		// Protects against version differences.
		// Shouldn't ever happen but do a sanity check for safety.
		if (t >= 0 && t < values.length)
			return values[t];
		else
			return null;
	}

	@OnlyIn(Dist.CLIENT)
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

	private static MutableComponent boolToText(boolean b) {
		return b ? new TextComponent("enabled").withStyle(ChatFormatting.DARK_GREEN) : new TextComponent("disabled").withStyle(ChatFormatting.RED);
	}
}
