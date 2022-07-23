package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.InstancedPart;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.instancing.GPUInstancer;
import com.jozufozu.flywheel.core.model.ModelSupplier;
import com.jozufozu.flywheel.mixin.LevelRendererAccessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.server.level.BlockDestructionProgress;

public class SadCrumbling {
//	public void renderCrumbling(LevelRenderer levelRenderer, ClientLevel level, PoseStack stack, Camera camera, Matrix4f projectionMatrix) {
//		var dataByStage = getDataByStage(levelRenderer, level);
//		if (dataByStage.isEmpty()) {
//			return;
//		}
//
//		var map = modelsToParts(dataByStage);
//		var stateSnapshot = GameStateRegistry.takeSnapshot();
//
////		Vec3 cameraPosition = camera.getPosition();
////		var camX = cameraPosition.x - originCoordinate.getX();
////		var camY = cameraPosition.y - originCoordinate.getY();
////		var camZ = cameraPosition.z - originCoordinate.getZ();
////
////		// don't want to mutate viewProjection
////		var vp = projectionMatrix.copy();
////		vp.multiplyWithTranslation((float) -camX, (float) -camY, (float) -camZ);
//
//		GlBuffer instanceBuffer = GlBuffer.requestPersistent(GlBufferType.ARRAY_BUFFER);
//
//		GlVertexArray crumblingVAO = new GlVertexArray();
//
//		crumblingVAO.bind();
//
//		// crumblingVAO.bindAttributes();
//
//		for (var entry : map.entrySet()) {
//			var model = entry.getKey();
//			var parts = entry.getValue();
//
//			if (parts.isEmpty()) {
//				continue;
//			}
//
//			StructType<?> structType = parts.get(0).type;
//
//			for (var meshEntry : model.get()
//					.entrySet()) {
//				Material material = meshEntry.getKey();
//				Mesh mesh = meshEntry.getValue();
//
//				MeshPool.BufferedMesh bufferedMesh = MeshPool.getInstance()
//						.get(mesh);
//
//				if (bufferedMesh == null || !bufferedMesh.isGpuResident()) {
//					continue;
//				}
//
//				material.renderType().setupRenderState();
//
//				CoreShaderInfoMap.CoreShaderInfo coreShaderInfo = CoreShaderInfoMap.CoreShaderInfo.get();
//
//
//				var program = Compile.PROGRAM.getProgram(new ProgramCompiler.Context(Formats.POS_TEX_NORMAL,
//						material, structType.getInstanceShader(), Components.CRUMBLING,
//						coreShaderInfo.getAdjustedAlphaDiscard(), coreShaderInfo.fogType(),
//						GameStateRegistry.takeSnapshot()));
//
//				program.bind();
//
//				// bufferedMesh.drawInstances();
//			}
//		}
//	}

	@NotNull
	private Map<ModelSupplier, List<InstancedPart>> modelsToParts(Int2ObjectMap<List<BlockEntityInstance<?>>> dataByStage) {
		var map = new HashMap<ModelSupplier, List<InstancedPart>>();

		for (var entry : dataByStage.int2ObjectEntrySet()) {
			RenderType currentLayer = ModelBakery.DESTROY_TYPES.get(entry.getIntKey());

			// something about when we call this means that the textures are not ready for use on the first frame they should appear
			if (currentLayer == null) {
				continue;
			}

			for (var blockEntityInstance : entry.getValue()) {

				for (var part : blockEntityInstance.getCrumblingParts()) {
					if (part.getOwner() instanceof GPUInstancer instancer) {

						// queue the instances for copying to the crumbling instance buffer
						map.computeIfAbsent(instancer.parent.getModel(), k -> new ArrayList<>()).add(part);
					}
				}
			}
		}
		return map;
	}

	@Nonnull
	private Int2ObjectMap<List<BlockEntityInstance<?>>> getDataByStage(LevelRenderer levelRenderer, ClientLevel level) {
		var destructionProgress = ((LevelRendererAccessor) levelRenderer).flywheel$getDestructionProgress();
		if (destructionProgress.isEmpty()) {
			return Int2ObjectMaps.emptyMap();
		}

		if (!(InstancedRenderDispatcher.getInstanceWorld(level)
				.getBlockEntities() instanceof BlockEntityInstanceManager beim)) {
			return Int2ObjectMaps.emptyMap();
		}

		var dataByStage = new Int2ObjectArrayMap<List<BlockEntityInstance<?>>>();

		for (var entry : destructionProgress.long2ObjectEntrySet()) {
			SortedSet<BlockDestructionProgress> progresses = entry.getValue();

			if (progresses == null || progresses.isEmpty()) {
				continue;
			}

			int progress = progresses.last()
					.getProgress();

			var data = dataByStage.computeIfAbsent(progress, $ -> new ArrayList<>());

			long pos = entry.getLongKey();

			beim.getCrumblingInstances(pos, data);
		}

		return dataByStage;
	}
}
