package com.jozufozu.flywheel.light;

import java.util.stream.Stream;

import com.jozufozu.flywheel.util.box.ImmutableBox;

import net.minecraft.world.level.LightLayer;

public class DummyLightUpdater extends LightUpdater {
	public static final DummyLightUpdater INSTANCE = new DummyLightUpdater();

	private DummyLightUpdater() {
		super(null);
	}

	@Override
	public void tick() {
		// noop
	}

	@Override
	public void addListener(LightListener listener) {
		// noop
	}

	@Override
	public void removeListener(LightListener listener) {
		// noop
	}

	@Override
	public void onLightUpdate(LightLayer type, long sectionPos) {
		// noop
	}

	@Override
	public void onLightPacket(int chunkX, int chunkZ) {
		// noop
	}

	@Override
	public Stream<ImmutableBox> getAllBoxes() {
		return Stream.empty();
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
}
