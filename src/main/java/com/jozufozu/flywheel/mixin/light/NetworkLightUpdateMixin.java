package com.jozufozu.flywheel.mixin.light;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.light.LightUpdater;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.play.server.SUpdateLightPacket;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;

@Mixin(ClientPlayNetHandler.class)
public class NetworkLightUpdateMixin {

	@Inject(at = @At("TAIL"), method = "handleUpdateLight")
	private void onLightPacket(SUpdateLightPacket packet, CallbackInfo ci) {
		RenderWork.enqueue(() -> {
			ClientWorld world = Minecraft.getInstance().world;

			if (world == null)
				return;

			int chunkX = packet.getChunkX();
			int chunkZ = packet.getChunkZ();

			Chunk chunk = world.getChunkProvider()
				.getChunk(chunkX, chunkZ, false);

			if (chunk != null) {
				chunk.getTileEntityMap()
						.values()
						.forEach(InstancedRenderDispatcher.getTiles(world)::onLightUpdate);

				Arrays.stream(chunk.getEntityLists())
						.flatMap(ClassInheritanceMultiMap::stream)
						.forEach(InstancedRenderDispatcher.getEntities(world)::onLightUpdate);
			}

			LightUpdater.getInstance()
				.onLightPacket(world, chunkX, chunkZ);
		});
	}
}
