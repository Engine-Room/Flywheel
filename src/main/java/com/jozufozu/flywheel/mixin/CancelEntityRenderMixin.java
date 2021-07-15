package com.jozufozu.flywheel.mixin;

import java.util.ArrayList;
import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;

@Mixin(WorldRenderer.class)
public class CancelEntityRenderMixin {

	@Group(name = "entityFilter", min = 1, max = 1)
	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;entitiesForRendering()Ljava/lang/Iterable;"))
	private Iterable<Entity> filterEntities(ClientWorld world) {
		Iterable<Entity> entities = world.entitiesForRendering();
		if (Backend.getInstance()
				.canUseInstancing()) {

			ArrayList<Entity> filtered = Lists.newArrayList(entities);

			InstancedRenderRegistry r = InstancedRenderRegistry.getInstance();
			filtered.removeIf(r::shouldSkipRender);

			return filtered;
		}
		return entities;
	}

	@Group(name = "entityFilter")
	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ClassInheritanceMultiMap;iterator()Ljava/util/Iterator;"))
	private Iterator<Entity> filterEntitiesOF(ClassInheritanceMultiMap<Entity> classInheritanceMultiMap) {
		if (Backend.getInstance()
				.canUseInstancing()) {

			ArrayList<Entity> filtered = Lists.newArrayList(classInheritanceMultiMap);

			InstancedRenderRegistry r = InstancedRenderRegistry.getInstance();
			filtered.removeIf(r::shouldSkipRender);

			return filtered.iterator();
		}
		return classInheritanceMultiMap.iterator();
	}
}
