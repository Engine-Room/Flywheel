package com.jozufozu.flywheel.config;


import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

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
		target = BooleanConfig.decode(buffer);
		directive = BooleanDirective.decode(buffer);
	}

	public void encode(FriendlyByteBuf buffer) {
		target.encode(buffer);
		directive.encode(buffer);
	}

	public void execute(Supplier<NetworkEvent.Context> ctx) {
		if (directive != null) {
			target.receiver.get()
					.accept(directive);
		}
		ctx.get()
				.setPacketHandled(true);
	}

}
