package dev.engine_room.flywheel.lib.visual;

import org.joml.FrustumIntersection;
import org.joml.Vector3f;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.EntityVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

/**
 * The layer between an {@link Entity} and the Flywheel backend.
 * <br>
 * <br> There are a few additional features that overriding classes can opt in to:
 * <ul>
 *     <li>{@link DynamicVisual}</li>
 *     <li>{@link TickableVisual}</li>
 *     <li>{@link LightUpdatedVisual}</li>
 *     <li>{@link ShaderLightVisual}</li>
 * </ul>
 * See the interfaces' documentation for more information about each one.
 *
 * <br> Implementing one or more of these will give an {@link AbstractEntityVisual} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <T> The type of {@link Entity}.
 */
public abstract class AbstractEntityVisual<T extends Entity> extends AbstractVisual implements EntityVisual<T> {
	protected final T entity;
	protected final EntityVisibilityTester visibilityTester;

	public AbstractEntityVisual(VisualizationContext ctx, T entity, float partialTick) {
		super(ctx, entity.level(), partialTick);
		this.entity = entity;
		visibilityTester = new EntityVisibilityTester(entity, ctx.renderOrigin(), 1.5f);
	}

	/**
	 * Calculate the distance squared between this visual and the given <em>level</em> position.
	 *
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return The distance squared between this visual and the given position.
	 */
	public double distanceSquared(double x, double y, double z) {
		return entity.distanceToSqr(x, y, z);
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link VisualizationManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the level matrix provided as a shader uniform accordingly.
	 *
	 * @return The position this visual should be rendered at to appear in the correct location.
	 */
	public Vector3f getVisualPosition() {
		Vec3 pos = entity.position();
		return new Vector3f((float) (pos.x - renderOrigin.getX()),
				(float) (pos.y - renderOrigin.getY()),
				(float) (pos.z - renderOrigin.getZ()));
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link VisualizationManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the level matrix provided as a shader uniform accordingly.
	 *
	 * @return The position this visual should be rendered at to appear in the correct location.
	 */
	public Vector3f getVisualPosition(float partialTick) {
		Vec3 pos = entity.position();
		return new Vector3f((float) (Mth.lerp(partialTick, entity.xOld, pos.x) - renderOrigin.getX()),
				(float) (Mth.lerp(partialTick, entity.yOld, pos.y) - renderOrigin.getY()),
				(float) (Mth.lerp(partialTick, entity.zOld, pos.z) - renderOrigin.getZ()));
	}

	public boolean isVisible(FrustumIntersection frustum) {
		return entity.noCulling || visibilityTester.check(frustum);
	}

	protected void relight(float partialTick, FlatLit... instances) {
		BlockPos pos = BlockPos.containing(entity.getLightProbePosition(partialTick));
		int blockLight = entity.isOnFire() ? 15 : level.getBrightness(LightLayer.BLOCK, pos);
		int skyLight = level.getBrightness(LightLayer.SKY, pos);
		int light = LightTexture.pack(blockLight, skyLight);
		FlatLit.relight(light, instances);
	}
}
