package com.jozufozu.flywheel.lib.visual;

import org.joml.FrustumIntersection;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.EntityVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.box.Box;
import com.jozufozu.flywheel.lib.box.MutableBox;
import com.jozufozu.flywheel.lib.light.TickingLightListener;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The layer between an {@link Entity} and the Flywheel backend.
 * *
 * <br><br> There are a few additional features that overriding classes can opt in to:
 * <ul>
 *     <li>{@link DynamicVisual}</li>
 *     <li>{@link TickableVisual}</li>
 * </ul>
 * See the interfaces' documentation for more information about each one.
 *
 * <br> Implementing one or more of these will give an {@link AbstractEntityVisual} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <T> The type of {@link Entity}.
 */
public abstract class AbstractEntityVisual<T extends Entity> extends AbstractVisual implements EntityVisual<T>, TickingLightListener {
	protected final T entity;
	protected final MutableBox bounds;
	protected final EntityVisibilityTester visibilityTester;

	public AbstractEntityVisual(VisualizationContext ctx, T entity) {
		super(ctx, entity.level());
		this.entity = entity;
		bounds = MutableBox.from(entity.getBoundingBox());
		visibilityTester = new EntityVisibilityTester(entity, ctx.renderOrigin());
	}

	/**
	 * Calculate the distance squared between this visual and the given <em>world</em> position.
	 *
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return The distance squared between this visual and the given position.
	 */
	public double distanceSquared(double x, double y, double z) {
		return entity.distanceToSqr(x, y, z);
	}

	@Override
	public Box getVolume() {
		return bounds;
	}

	@Override
	public boolean tickLightListener() {
		AABB boundsNow = entity.getBoundingBox();

		if (bounds.sameAs(boundsNow)) {
			return false;
		}

		bounds.assign(boundsNow);

		updateLight();

		return true;
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link VisualizationManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the world matrix provided as a shader uniform accordingly.
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
	 * shift the world matrix provided as a shader uniform accordingly.
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
}
