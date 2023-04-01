package com.jozufozu.flywheel.lib.light;

import java.util.stream.Stream;

import com.jozufozu.flywheel.lib.box.ImmutableBox;

import net.minecraft.core.SectionPos;
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
	public void onLightUpdate(LightLayer type, SectionPos pos) {
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
