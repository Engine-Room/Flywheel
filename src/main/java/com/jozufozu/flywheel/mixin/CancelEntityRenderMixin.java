package com.jozufozu.flywheel.mixin;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public class CancelEntityRenderMixin {

	// TODO: Don't use redirect
	@Group(name = "entityFilter", min = 1, max = 1)
	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
	private Iterable<Entity> filterEntities(ClientLevel world) {
		Iterable<Entity> entities = world.entitiesForRendering();
		if (Backend.isOn()) {
			ArrayList<Entity> filtered = Lists.newArrayList(entities);

			filtered.removeIf(InstancedRenderRegistry::shouldSkipRender);

			return filtered;
		}
		return entities;
	}

//	@Group(name = "entityFilter")
//	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ClassInstanceMultiMap;iterator()Ljava/util/Iterator;"))
//	private Iterator<Entity> filterEntitiesOF(ClassInstanceMultiMap<Entity> classInheritanceMultiMap) {
//		if (Backend.getInstance()
//				.canUseInstancing()) {
//
//			ArrayList<Entity> filtered = Lists.newArrayList(classInheritanceMultiMap);
//
//			InstancedRenderRegistry r = InstancedRenderRegistry.getInstance();
//			filtered.removeIf(r::shouldSkipRender);
//
//			return filtered.iterator();
//		}
//		return classInheritanceMultiMap.iterator();
//	}
}
