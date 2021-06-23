package com.jozufozu.flywheel.mixin;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class CancelEntityRenderMixin {

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;entitiesForRendering()Ljava/lang/Iterable;"))
	private Iterable<Entity> filterEntities(ClientWorld world) {
		Iterable<Entity> entities = world.entitiesForRendering();
		if (Backend.getInstance().canUseInstancing()) {

			ArrayList<Entity> filtered = Lists.newArrayList(entities);
			filtered.removeIf(tile -> tile instanceof IInstanceRendered && !((IInstanceRendered) tile).shouldRenderNormally());

			return filtered;
		}
		return entities;
	}
}
