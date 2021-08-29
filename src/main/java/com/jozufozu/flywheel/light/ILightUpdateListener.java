package com.jozufozu.flywheel.light;

import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;

/**
 * Anything can implement this, implementors should call {@link #startListening}
 * appropriately to make sure they get the updates they want.
 */
public interface ILightUpdateListener {

	Volume getVolume();

	ListenerStatus status();

	default void startListening() {
		LightUpdater.getInstance().addListener(this);
	}

	/**
	 * Called when a light updates in a chunk the implementor cares about.
	 */
	void onLightUpdate(IBlockDisplayReader world, LightType type, GridAlignedBB changed);

	/**
	 * Called when the server sends light data to the client.
	 *
	 */
	default void onLightPacket(IBlockDisplayReader world, int chunkX, int chunkZ) {
		GridAlignedBB changedVolume = GridAlignedBB.from(chunkX, chunkZ);

		onLightUpdate(world, LightType.BLOCK, changedVolume);

		onLightUpdate(world, LightType.SKY, changedVolume);
	}
}
