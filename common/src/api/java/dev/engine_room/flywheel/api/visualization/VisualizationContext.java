package dev.engine_room.flywheel.api.visualization;

import dev.engine_room.flywheel.api.backend.BackendImplemented;
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
	 * <p>For a Visual to appear in the correct position in the world,
	 * it must render at its actual world position minus this renderOrigin.
	 * <br>i.e. {@code be.getBlockPos() - visualizationContext.renderOrigin()}</p>
	 *
	 * <p>This exists to prevent floating point precision issues
	 * when the camera is far away from the level's origin.</p>
	 *
	 * @return The origin of the renderer as a level position.
	 */
	Vec3i renderOrigin();

	/**
	 * Create a new embedding to compose visuals.
	 *
	 * @param renderOrigin The renderOrigin the embedding will appear to have.
	 * @return The embedding.
	 * @see VisualEmbedding
	 */
	VisualEmbedding createEmbedding(Vec3i renderOrigin);
}
