package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;

import dev.engine_room.flywheel.api.material.Material;
import net.minecraft.client.resources.model.geometry.BakedQuad;

@ApiStatus.Internal
public class NeoforgeMeshEmitter extends MeshEmitter {
	NeoforgeMeshEmitter(ByteBufferBuilderStack byteBufferBuilderStack, net.minecraft.client.renderer.chunk.ChunkSectionLayer chunkLayer) {
		super(byteBufferBuilderStack, chunkLayer);
	}

	public void put(PoseStack poseStack, float x, float y, float z, BakedQuad quad, QuadInstance instance, boolean defaultAo) {
		BufferBuilder bufferBuilder = getBuffer(quad, defaultAo);
		if (bufferBuilder != null) {
			poseStack.pushPose();
			poseStack.translate(x, y, z);
			bufferBuilder.putBakedQuad(poseStack.last(), quad, instance);
			poseStack.popPose();
		}
	}

	@Nullable
	private BufferBuilder getBuffer(BakedQuad quad, boolean defaultAo) {
		BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
		Material key = blockMaterialFunction.apply(materialInfo.layer(), materialInfo.shade(), materialInfo.ambientOcclusion() && defaultAo);
		if (key != null) {
			return getBuffer(key);
		} else {
			return null;
		}
	}
}
