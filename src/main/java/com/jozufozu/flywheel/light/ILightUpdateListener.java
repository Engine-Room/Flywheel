package com.jozufozu.flywheel.light;

import net.minecraft.world.level.LightLayer;

public interface ILightUpdateListener {

	ImmutableBox getVolume();

	ListenerStatus status();

	/**
	 * Called when a light updates in a chunk the implementor cares about.
	 */
	void onLightUpdate(LightProvider world, LightLayer type, ImmutableBox changed);

	/**
	 * Called when the server sends light data to the client.
	 *
	 */
	default void onLightPacket(LightProvider world, int chunkX, int chunkZ) {
		GridAlignedBB changedVolume = GridAlignedBB.from(chunkX, chunkZ);

		onLightUpdate(world, LightLayer.BLOCK, changedVolume);

		onLightUpdate(world, LightLayer.SKY, changedVolume);
	}
}
