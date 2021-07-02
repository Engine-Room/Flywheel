package com.jozufozu.flywheel.mixin.light;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.ChunkUtil;

import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientChunkProvider.class)
public abstract class LightUpdateMixin extends AbstractChunkProvider {

	/**
	 * JUSTIFICATION: This method is called after a lighting tick once per subchunk where a
	 * lighting change occurred that tick. On the client, Minecraft uses this method to inform
	 * the rendering system that it needs to redraw a chunk. It does all that work asynchronously,
	 * and we should too.
	 */
	@Inject(at = @At("HEAD"), method = "markLightChanged")
	private void onLightUpdate(LightType type, SectionPos pos, CallbackInfo ci) {
		ClientChunkProvider thi = ((ClientChunkProvider) (Object) this);
		ClientWorld world = (ClientWorld) thi.getWorld();

		Chunk chunk = thi.getChunk(pos.getSectionX(), pos.getSectionZ(), false);

		int sectionY = pos.getSectionY();

		if (ChunkUtil.isValidSection(chunk, sectionY)) {
			InstanceManager<TileEntity> tiles = InstancedRenderDispatcher.getTiles(world);
			InstanceManager<Entity> entities = InstancedRenderDispatcher.getEntities(world);

			chunk.getTileEntityMap()
					.entrySet()
					.stream()
					.filter(entry -> SectionPos.toChunk(entry.getKey()
																.getY()) == sectionY)
					.map(Map.Entry::getValue)
					.forEach(tiles::onLightUpdate);

			chunk.getEntityLists()[sectionY].forEach(entities::onLightUpdate);
		}

		LightUpdater.getInstance()
				.onLightUpdate(world, type, pos.asLong());
	}
}
