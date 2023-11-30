package com.jozufozu.flywheel.lib.visual;

import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.lib.math.MoreMath;

import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * A helper class for testing whether an Entity is visible.
 * <p>
 * The last visible AABB is also checked to prevent the Entity from freezing when it goes offscreen.
 */
public class EntityVisibilityTester {
	private final Entity entity;
	private final Vec3i renderOrigin;
	private final float scale;
	@Nullable
	private AABB lastVisibleAABB;

	/**
	 * Create a new EntityVisibilityTester.
	 *
	 * @param entity       The Entity to test.
	 * @param renderOrigin The render origin according to the VisualizationContext.
	 * @param scale        Multiplier for the Entity's size, can be used to adjust for when
	 *                     an entity's model is larger than its hitbox.
	 */
	public EntityVisibilityTester(Entity entity, Vec3i renderOrigin, float scale) {
		this.entity = entity;
		this.renderOrigin = renderOrigin;
		this.scale = scale;
	}

	/**
	 * Check whether the Entity is visible.
	 *
	 * @param frustum The frustum to test against.
	 * @return {@code true} if the Entity is visible, {@code false} otherwise.
	 */
	public boolean check(FrustumIntersection frustum) {
		AABB aabb = entity.getBoundingBoxForCulling();

		boolean visible = adjustAndTestAABB(frustum, aabb);

		if (!visible && lastVisibleAABB != null && lastVisibleAABB != aabb) {
			// If the entity isn't visible, check the last visible AABB as well.
			// This is to avoid Entities freezing when the go offscreen.
			visible = adjustAndTestAABB(frustum, lastVisibleAABB);
		}

		if (visible) {
			lastVisibleAABB = aabb;
		}
		return visible;
	}

	private boolean adjustAndTestAABB(FrustumIntersection frustum, AABB aabb) {
		float x = (float) Mth.lerp(0.5D, aabb.minX, aabb.maxX) - renderOrigin.getX();
		float y = (float) Mth.lerp(0.5D, aabb.minY, aabb.maxY) - renderOrigin.getY();
		float z = (float) Mth.lerp(0.5D, aabb.minZ, aabb.maxZ) - renderOrigin.getZ();
		float maxSize = (float) Math.max(aabb.getXsize(), Math.max(aabb.getYsize(), aabb.getZsize()));
		return frustum.testSphere(x, y, z, maxSize * MoreMath.SQRT_3_OVER_2 * scale);
	}
}
