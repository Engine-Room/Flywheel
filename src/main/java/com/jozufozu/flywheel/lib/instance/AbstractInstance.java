package com.jozufozu.flywheel.lib.instance;

import java.util.stream.Stream;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;
import com.jozufozu.flywheel.lib.light.LightListener;
import com.jozufozu.flywheel.lib.light.LightUpdater;
import com.jozufozu.flywheel.lib.struct.FlatLit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public abstract class AbstractInstance implements Instance, LightListener {
	protected final InstancerProvider instancerProvider;
	protected final Vec3i renderOrigin;
	protected final Level level;

	protected boolean deleted = false;

	public AbstractInstance(InstanceContext ctx, Level level) {
		this.instancerProvider = ctx.instancerProvider();
		this.renderOrigin = ctx.renderOrigin();
		this.level = level;
	}

	@Override
	public void init() {
		LightUpdater.get(level).addListener(this);
		updateLight();
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
	public void onLightUpdate(LightLayer type, SectionPos pos) {
		updateLight();
	}

	@Override
	public boolean isInvalid() {
		return deleted;
	}

	protected void relight(BlockPos pos, FlatLit<?>... parts) {
		relight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos), parts);
	}

	protected void relight(int block, int sky, FlatLit<?>... parts) {
		for (FlatLit<?> part : parts) {
			part.setLight(block, sky);
		}
	}

	protected <L extends FlatLit<?>> void relight(BlockPos pos, Stream<L> parts) {
		relight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos), parts);
	}

	protected <L extends FlatLit<?>> void relight(int block, int sky, Stream<L> parts) {
		parts.forEach(model -> model.setLight(block, sky));
	}
}
