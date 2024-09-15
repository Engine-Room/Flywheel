package dev.engine_room.flywheel.lib.visual;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.visual.Visual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

public abstract class AbstractVisual implements Visual {
	/**
	 * The visualization context used to construct this visual.
	 * <br>
	 * Useful for passing to child visuals.
	 */
	protected final VisualizationContext visualizationContext;
	protected final Level level;

	protected boolean deleted = false;

	public AbstractVisual(VisualizationContext ctx, Level level, float partialTick) {
		this.visualizationContext = ctx;
		this.level = level;
	}

	@Override
	public void update(float partialTick) {
	}

    protected abstract void _delete();

	protected InstancerProvider instancerProvider() {
		return visualizationContext.instancerProvider();
	}

	protected Vec3i renderOrigin() {
		return visualizationContext.renderOrigin();
	}

	@Override
	public final void delete() {
		if (deleted) {
			return;
		}

		_delete();
		deleted = true;
	}
}
