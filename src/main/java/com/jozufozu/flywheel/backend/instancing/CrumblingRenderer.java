package com.jozufozu.flywheel.backend.instancing;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE4;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.crumbling.CrumblingInstanceManager;
import com.jozufozu.flywheel.core.crumbling.CrumblingMaterialManager;
import com.jozufozu.flywheel.core.crumbling.CrumblingProgram;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Responsible for rendering the block breaking overlay for instanced tiles.
 */
@Environment(EnvType.CLIENT)
public class CrumblingRenderer {

	private static final LazyValue<MaterialManager<CrumblingProgram>> materialManager = new LazyValue<>(() -> new CrumblingMaterialManager(Contexts.CRUMBLING));
	private static final LazyValue<InstanceManager<TileEntity>> manager = new LazyValue<>(() -> new CrumblingInstanceManager(materialManager.getValue()));

	private static final RenderType crumblingLayer = ModelBakery.BLOCK_DESTRUCTION_RENDER_LAYERS.get(0);

	public static void renderBreaking(ClientWorld world, Matrix4f viewProjection, double cameraX, double cameraY, double cameraZ) {
		if (!Backend.getInstance()
				.canUseInstancing(world)) return;

		Int2ObjectMap<List<TileEntity>> activeStages = getActiveStageTiles(world);

		if (activeStages.isEmpty()) return;

		InstanceManager<TileEntity> renderer = manager.getValue();

		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		ActiveRenderInfo info = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();

		MaterialManager<CrumblingProgram> materials = materialManager.getValue();
		crumblingLayer.startDrawing();

		for (Int2ObjectMap.Entry<List<TileEntity>> stage : activeStages.int2ObjectEntrySet()) {
			int i = stage.getIntKey();
			Texture breaking = textureManager.getTexture(ModelBakery.BLOCK_DESTRUCTION_STAGE_TEXTURES.get(i));

			// something about when we call this means that the textures are not ready for use on the first frame they should appear
			if (breaking != null) {
				stage.getValue().forEach(renderer::add);

				renderer.beginFrame(info);

				glActiveTexture(GL_TEXTURE4);
				glBindTexture(GL_TEXTURE_2D, breaking.getGlTextureId());
				materials.render(RenderType.getCutoutMipped(), viewProjection, cameraX, cameraY, cameraZ);

				renderer.invalidate();
			}

		}

		crumblingLayer.endDrawing();

		glActiveTexture(GL_TEXTURE0);
		Texture breaking = textureManager.getTexture(ModelBakery.BLOCK_DESTRUCTION_STAGE_TEXTURES.get(0));
		if (breaking != null) glBindTexture(GL_TEXTURE_2D, breaking.getGlTextureId());
	}

	/**
	 * Associate each breaking stage with a list of all tile entities at that stage.
	 */
	private static Int2ObjectMap<List<TileEntity>> getActiveStageTiles(ClientWorld world) {
		Long2ObjectMap<SortedSet<DestroyBlockProgress>> breakingProgressions = Minecraft.getInstance().worldRenderer.blockBreakingProgressions;

		Int2ObjectMap<List<TileEntity>> breakingEntities = new Int2ObjectArrayMap<>();

		for (Long2ObjectMap.Entry<SortedSet<DestroyBlockProgress>> entry : breakingProgressions.long2ObjectEntrySet()) {
			BlockPos breakingPos = BlockPos.fromLong(entry.getLongKey());

			SortedSet<DestroyBlockProgress> progresses = entry.getValue();
			if (progresses != null && !progresses.isEmpty()) {
				int blockDamage = progresses.last()
						.getPartialBlockDamage();

				TileEntity tileEntity = world.getTileEntity(breakingPos);

				if (tileEntity != null) {
					List<TileEntity> tileEntities = breakingEntities.computeIfAbsent(blockDamage, $ -> new ArrayList<>());
					tileEntities.add(tileEntity);
				}
			}
		}

		return breakingEntities;
	}

	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ClientWorld world = event.getWorld();
		if (Backend.getInstance()
				.canUseInstancing() && world != null) {
			materialManager.getValue().delete();
		}
	}
}
