package dev.engine_room.flywheel.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {
	@Invoker("affectedByCulling")
	<T extends Entity> boolean flywheel$affectedByCulling(T entity);

	@Invoker("getBoundingBoxForCulling")
	<T extends Entity> AABB flywheel$getBoundingBoxForCulling(T entity);
}
