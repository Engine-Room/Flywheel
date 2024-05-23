package dev.engine_room.flywheel.api.visualization;

import dev.engine_room.flywheel.api.BackendImplemented;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import net.minecraft.core.Vec3i;

/**
 * A context object passed on visual creation.
 */
@BackendImplemented
public interface VisualizationContext {
	/**
	 * @return The {@link InstancerProvider} that the visual can use to get instancers to render models.
	 */
	InstancerProvider instancerProvider();

	/**
	 * All models render as if this position is (0, 0, 0).
	 *
	 * @return The origin of the renderer as a level position.
	 */
	Vec3i renderOrigin();

	VisualEmbedding createEmbedding();
}
