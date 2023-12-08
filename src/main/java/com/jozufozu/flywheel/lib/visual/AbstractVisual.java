package com.jozufozu.flywheel.lib.visual;

import java.util.stream.Stream;

import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.visual.Visual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.FlatLit;
import com.jozufozu.flywheel.lib.light.LightListener;
import com.jozufozu.flywheel.lib.light.LightUpdater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public abstract class AbstractVisual implements Visual, LightListener {
	protected final InstancerProvider instancerProvider;
	protected final Vec3i renderOrigin;
	protected final Level level;

	protected boolean deleted = false;

	public AbstractVisual(VisualizationContext ctx, Level level) {
		this.instancerProvider = ctx.instancerProvider();
		this.renderOrigin = ctx.renderOrigin();
		this.level = level;
	}

	@Override
	public void init(float partialTick) {
		LightUpdater.get(level).addListener(this);
		updateLight();
	}

	@Override
	public void update(float partialTick) {
	}

	@Override
	public boolean shouldReset() {
		return false;
	}

	/**
	 * Called after initialization and when a light update occurs in the world.
	 * <br>
	 * If your instances need it, update light here.
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

	protected void relight(BlockPos pos, FlatLit... instances) {
		relight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos), instances);
	}

	protected void relight(int block, int sky, FlatLit... instances) {
		for (FlatLit instance : instances) {
			instance.setLight(block, sky);
			instance.handle()
					.setChanged();
		}
	}

	protected void relight(BlockPos pos, Stream<? extends FlatLit> instances) {
		relight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos), instances);
	}

	protected void relight(int block, int sky, Stream<? extends FlatLit> instances) {
		instances.forEach(model -> model.setLight(block, sky)
				.handle()
				.setChanged());
	}
}
