package com.jozufozu.flywheel.mixin.light;

import java.util.Arrays;

import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.light.LightUpdater;

import net.minecraft.client.Minecraft;

@Mixin(ClientPacketListener.class)
public class NetworkLightUpdateMixin {

	@Inject(at = @At("TAIL"), method = "handleLightUpdatePacked")
	private void onLightPacket(ClientboundLightUpdatePacket packet, CallbackInfo ci) {
		RenderWork.enqueue(() -> {
			ClientLevel world = Minecraft.getInstance().level;

			if (world == null) return;

			int chunkX = packet.getX();
			int chunkZ = packet.getZ();

			LevelChunk chunk = world.getChunkSource()
					.getChunk(chunkX, chunkZ, false);

			if (chunk != null) {
				InstanceManager<BlockEntity> tiles = InstancedRenderDispatcher.getTiles(world);
				InstanceManager<Entity> entities = InstancedRenderDispatcher.getEntities(world);

				chunk.getBlockEntities()
						.values()
						.forEach(tiles::onLightUpdate);

				Arrays.stream(chunk.getEntitySections())
						.flatMap(ClassInstanceMultiMap::stream)
						.forEach(entities::onLightUpdate);
			}

			LightUpdater.getInstance()
					.onLightPacket(world, chunkX, chunkZ);
		});
	}
}
