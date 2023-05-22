package com.jozufozu.flywheel.lib.visual;

import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;

import net.minecraft.core.Vec3i;
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
	@Nullable
	private AABB lastVisibleAABB;

	public EntityVisibilityTester(Entity entity, Vec3i renderOrigin) {
		this.entity = entity;

		this.renderOrigin = renderOrigin;
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
		float minX = (float) (aabb.minX - renderOrigin.getX() - 0.5);
		float minY = (float) (aabb.minY - renderOrigin.getY() - 0.5);
		float minZ = (float) (aabb.minZ - renderOrigin.getZ() - 0.5);
		float maxX = (float) (aabb.maxX - renderOrigin.getX() + 0.5);
		float maxY = (float) (aabb.maxY - renderOrigin.getY() + 0.5);
		float maxZ = (float) (aabb.maxZ - renderOrigin.getZ() + 0.5);
		return frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
