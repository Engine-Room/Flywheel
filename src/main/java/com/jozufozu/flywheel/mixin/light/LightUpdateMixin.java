package com.jozufozu.flywheel.mixin.light;

import java.util.Map;

import com.jozufozu.flywheel.mixin.LevelAccessor;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;

import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.ChunkUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(ClientChunkCache.class)
public abstract class LightUpdateMixin extends ChunkSource {

	/**
	 * JUSTIFICATION: This method is called after a lighting tick once per subchunk where a
	 * lighting change occurred that tick. On the client, Minecraft uses this method to inform
	 * the rendering system that it needs to redraw a chunk. It does all that work asynchronously,
	 * and we should too.
	 */
	@Inject(at = @At("HEAD"), method = "onLightUpdate")
	private void onLightUpdate(LightLayer type, SectionPos pos, CallbackInfo ci) {
		ClientChunkCache thi = ((ClientChunkCache) (Object) this);
		ClientLevel world = (ClientLevel) thi.getLevel();

		LevelChunk chunk = thi.getChunk(pos.x(), pos.z(), false);

		int sectionY = pos.y();

		if (ChunkUtil.isValidSection(chunk, sectionY)) {
			InstanceManager<BlockEntity> tiles = InstancedRenderDispatcher.getTiles(world);
			InstanceManager<Entity> entities = InstancedRenderDispatcher.getEntities(world);

			chunk.getBlockEntities()
					.entrySet()
					.stream()
					.filter(entry -> SectionPos.blockToSectionCoord(entry.getKey()
																.getY()) == sectionY)
					.map(Map.Entry::getValue)
					.forEach(tiles::onLightUpdate);

			((LevelAccessor) world).invokeGetEntities().getAll().forEach(entity -> {
				if (SectionPos.of(entity) != pos) return;
				entities.onLightUpdate(entity);
			});
		}

		LightUpdater.getInstance()
				.onLightUpdate(world, type, pos.asLong());
	}
}
