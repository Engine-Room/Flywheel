package com.jozufozu.flywheel.lib.instance;

import java.util.Arrays;
import java.util.stream.Stream;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instancer.FlatLit;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;
import com.jozufozu.flywheel.lib.light.LightListener;
import com.jozufozu.flywheel.lib.light.LightUpdater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public abstract class AbstractInstance implements Instance, LightListener {
	protected final InstancerProvider instancerManager;
	public final Level level;
	protected boolean deleted = false;

	public AbstractInstance(InstancerProvider instancerManager, Level level) {
		this.instancerManager = instancerManager;
		this.level = level;
	}

	@Override
	public void init() {
		updateLight();
		LightUpdater.get(level).addListener(this);
	}

	@Override
	public void update() {
	}

	@Override
	public boolean shouldReset() {
		return false;
	}

	/**
	 * Called after construction and when a light update occurs in the world.
	 *
	 * <br> If your model needs it, update light here.
	 */
	public void updateLight() {
	}

	protected abstract void _delete();

	@Override
	public final void delete() {
		if (deleted) {
			return;
		}

		_delete();
		deleted = true;
	}

	@Override
	public boolean isInvalid() {
		return deleted;
	}

	@Override
	public void onLightUpdate(LightLayer type, SectionPos pos) {
		updateLight();
	}

	protected void relight(BlockPos pos, FlatLit<?>... models) {
		relight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos), models);
	}

	protected <L extends FlatLit<?>> void relight(BlockPos pos, Stream<L> models) {
		relight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos), models);
	}

	protected void relight(int block, int sky, FlatLit<?>... models) {
		relight(block, sky, Arrays.stream(models));
	}

	protected <L extends FlatLit<?>> void relight(int block, int sky, Stream<L> models) {
		models.forEach(model -> model.setBlockLight(block)
				.setSkyLight(sky));
	}

}
