package com.jozufozu.flywheel.config;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Thanks, @zelophed
 */
public class SConfigureBooleanPacket {

	private final BooleanConfig target;
	private final BooleanDirective directive;

	public SConfigureBooleanPacket(BooleanConfig target, BooleanDirective directive) {
		this.target = target;
		this.directive = directive;
	}

	public SConfigureBooleanPacket(FriendlyByteBuf buffer) {
		target = BooleanConfig.values()[buffer.readByte()];
		directive = BooleanDirective.values()[buffer.readByte()];
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeByte(target.ordinal());
		buffer.writeByte(directive.ordinal());
	}

	public void execute(Supplier<NetworkEvent.Context> ctx) {
		target.receiver.get()
				.accept(directive);
		ctx.get()
				.setPacketHandled(true);
	}

}
