package com.jozufozu.flywheel.light;

import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;

import net.minecraft.world.level.LightLayer;

/**
 * Implementors of this interface may choose to subscribe to light updates by calling
 * {@link LightUpdater#addListener(LightListener)}.<p>
 *
 * It is the responsibility of the implementor to keep a reference to the level an object is contained in.
 */
public interface LightListener {

	ImmutableBox getVolume();

	/**
	 * Check the status of the light listener.
	 * @return {@code true} if the listener is invalid/removed/deleted,
	 * 	       and should no longer receive updates.
	 */
	boolean isListenerInvalid();

	/**
	 * Called when a light updates in a chunk the implementor cares about.
	 */
	void onLightUpdate(LightLayer type, ImmutableBox changed);

	/**
	 * Called when the server sends light data to the client.
	 *
	 */
	default void onLightPacket(int chunkX, int chunkZ) {
		GridAlignedBB changedVolume = GridAlignedBB.from(chunkX, chunkZ);

		onLightUpdate(LightLayer.BLOCK, changedVolume);

		onLightUpdate(LightLayer.SKY, changedVolume);
	}
}
