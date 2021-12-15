package com.jozufozu.flywheel.config;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SConfigureEnginePacket {

	private final FlwEngine type;

	public SConfigureEnginePacket() {
		type = null;
	}

	public SConfigureEnginePacket(FlwEngine type) {
		this.type = type;
	}

	public SConfigureEnginePacket(FriendlyByteBuf buffer) {
		type = FlwEngine.decode(buffer);
	}

	public void encode(FriendlyByteBuf buffer) {
		if (type != null)
			type.encode(buffer);
		else
			buffer.writeByte(-1);
	}

	public void execute(Supplier<NetworkEvent.Context> ctx) {
		if (type != null) {
			type.switchTo();
		}
		ctx.get()
				.setPacketHandled(true);
	}
}
