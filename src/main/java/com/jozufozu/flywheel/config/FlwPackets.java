package com.jozufozu.flywheel.config;

import com.jozufozu.flywheel.Flywheel;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class FlwPackets {
	public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Flywheel.ID, "network");
	public static final String NETWORK_VERSION = new ResourceLocation(Flywheel.ID, "1").toString();
	public static SimpleChannel channel;

	public static void registerPackets() {
		channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
				.serverAcceptedVersions(NETWORK_VERSION::equals)
				.clientAcceptedVersions(NETWORK_VERSION::equals)
				.networkProtocolVersion(() -> NETWORK_VERSION)
				.simpleChannel();

		channel.messageBuilder(SConfigureBooleanPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
				.decoder(SConfigureBooleanPacket::new)
				.encoder(SConfigureBooleanPacket::encode)
				.consumer(SConfigureBooleanPacket::execute)
				.add();
	}
}
