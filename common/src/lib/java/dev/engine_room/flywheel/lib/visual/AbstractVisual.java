package dev.engine_room.flywheel.lib.visual;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.visual.Visual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public abstract class AbstractVisual implements Visual {
	/**
	 * The visualization context used to construct this visual.
	 * <br>
	 * Useful for passing to child visuals.
	 */
	protected final VisualizationContext visualizationContext;
	protected final InstancerProvider instancerProvider;
	protected final Vec3i renderOrigin;
	protected final Level level;

	protected boolean deleted = false;

	public AbstractVisual(VisualizationContext ctx, Level level) {
		this.visualizationContext = ctx;
		this.instancerProvider = ctx.instancerProvider();
		this.renderOrigin = ctx.renderOrigin();
		this.level = level;
	}

	@Override
	public void update(float partialTick) {
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

	protected void relight(BlockPos pos, @Nullable FlatLit... instances) {
		relight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos), instances);
	}

	protected void relight(int block, int sky, @Nullable FlatLit... instances) {
		for (FlatLit instance : instances) {
			if (instance == null) {
				continue;
			}

			instance.light(block, sky)
					.handle()
					.setChanged();
		}
	}

	protected void relight(BlockPos pos, Stream<? extends @Nullable FlatLit> instances) {
		relight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos), instances);
	}

	protected void relight(int block, int sky, Stream<? extends @Nullable FlatLit> instances) {
		instances.filter(Objects::nonNull)
				.forEach(instance -> instance.light(block, sky)
				.handle()
				.setChanged());
	}

	protected void relight(BlockPos pos, Iterable<? extends @Nullable FlatLit> instances) {
		relight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos), instances);
	}

	protected void relight(int block, int sky, Iterable<? extends @Nullable FlatLit> instances) {
		for (FlatLit instance : instances) {
			if (instance == null) {
				continue;
			}
			instance.light(block, sky)
					.handle()
					.setChanged();
		}
	}
}
