package com.jozufozu.flywheel.mixin;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.impl.visualization.VisualizationHelper;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

@Mixin(ClientLevel.class)
abstract class ClientLevelMixin  {
	@Inject(method = "entitiesForRendering()Ljava/lang/Iterable;", at = @At("RETURN"), cancellable = true)
	private void flywheel$filterEntities(CallbackInfoReturnable<Iterable<Entity>> cir) {
		if (!VisualizationManager.supportsVisualization((ClientLevel) (Object) this)) {
			return;
		}

		Iterable<Entity> entities = cir.getReturnValue();
		ArrayList<Entity> filtered = Lists.newArrayList(entities);

		filtered.removeIf(VisualizationHelper::shouldSkipRender);

		cir.setReturnValue(filtered);
	}
}
