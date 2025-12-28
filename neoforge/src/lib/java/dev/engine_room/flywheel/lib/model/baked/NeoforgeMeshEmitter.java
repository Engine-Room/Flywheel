package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.api.material.Material;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

@ApiStatus.Internal
public class NeoforgeMeshEmitter extends MeshEmitter implements VertexConsumer {
	private final ChunkSectionLayer chunkSectionLayer;

	private boolean defaultAo;

	NeoforgeMeshEmitter(ByteBufferBuilderStack byteBufferBuilderStack, ChunkSectionLayer chunkSectionLayer) {
		super(byteBufferBuilderStack, chunkSectionLayer);
		this.chunkSectionLayer = chunkSectionLayer;
	}

	/**
	 * Some mods, like FramedBlocks, have custom hooks to determine the default AO. This method is invoked a second time
	 * from within a mixin to {@link ModelBlockRenderer} after the accurate value is computed, so we don't need to
	 * support those custom hooks manually. It is possible that the mixin injector will never run (primarily due to
	 * implementations of Fabric Renderer API on Forge, like Indigo in Forgified Fabric API), so we always compute the
	 * value manually beforehand too.
	 */
	public void prepareForModelLayer(boolean defaultAo) {
		this.defaultAo = defaultAo;
	}

	@Nullable
	private BufferBuilder getBuffer(boolean shade, boolean ao) {
		Material key = blockMaterialFunction.apply(chunkSectionLayer, shade, ao);
		if (key != null) {
			return getBuffer(key);
		} else {
			return null;
		}
	}

	@Nullable
	private BufferBuilder getBuffer(BakedQuad quad) {
		boolean shade = quad.shade();
		boolean ao = quad.hasAmbientOcclusion() && defaultAo;
		return getBuffer(shade, ao);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay);
		}
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setColor(int color) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setLineWidth(float f) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}
}
