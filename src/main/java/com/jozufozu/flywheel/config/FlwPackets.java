package com.jozufozu.flywheel.config;

import com.jozufozu.flywheel.Flywheel;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class FlwPackets {
	public static final ResourceLocation CHANNEL_NAME = Flywheel.rl("main");
	public static final String NETWORK_VERSION = String.valueOf(1);
	public static SimpleChannel channel;

	public static void registerPackets() {
		channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
				.serverAcceptedVersions(NETWORK_VERSION::equals)
				.clientAcceptedVersions(NETWORK_VERSION::equals)
				.networkProtocolVersion(() -> NETWORK_VERSION)
				.simpleChannel();

		int id = 0;

		channel.messageBuilder(SConfigureBooleanPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
				.decoder(SConfigureBooleanPacket::new)
				.encoder(SConfigureBooleanPacket::encode)
				.consumer(SConfigureBooleanPacket::execute)
				.add();

		channel.messageBuilder(SConfigureEnginePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
				.decoder(SConfigureEnginePacket::new)
				.encoder(SConfigureEnginePacket::encode)
				.consumer(SConfigureEnginePacket::execute)
				.add();
	}
}
