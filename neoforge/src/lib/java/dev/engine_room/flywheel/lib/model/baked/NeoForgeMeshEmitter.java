package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.neoforged.neoforge.client.model.quad.MutableQuad;

@ApiStatus.Internal
public class NeoForgeMeshEmitter extends MeshEmitter {
	private boolean ao;

	NeoForgeMeshEmitter(ByteBufferBuilderStack byteBufferBuilderStack, ChunkSectionLayer chunkSectionLayer) {
		super(byteBufferBuilderStack, chunkSectionLayer);
	}

	// Called from ModelBlockRendererMixin if AO is on for the model before each part is buffered
	public void prepareForPart(boolean ao) {
		this.ao = ao;
	}

	@Nullable
	private BufferBuilder getBuffer(BakedQuad quad) {
		boolean shade = quad.materialInfo().shade();
		boolean ao = quad.materialInfo().ambientOcclusion() && this.ao;
		return getBuffer(shade, ao);
	}

	@Override
	public void putBakedQuad(Pose pose, BakedQuad quad, QuadInstance instance) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBakedQuad(pose, quad, instance);
		}
	}

	@Override
	public void putBlockBakedQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBlockBakedQuad(x, y, z, quad, instance);
		}
	}

	@Override
	public void putMutableQuad(Pose pose, MutableQuad quad, QuadInstance instance) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBakedQuad/putBlockBakedQuad!");
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBakedQuad/putBlockBakedQuad!");
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBakedQuad/putBlockBakedQuad!");
	}

	@Override
	public VertexConsumer setColor(int color) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBakedQuad/putBlockBakedQuad!");
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBakedQuad/putBlockBakedQuad!");
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBakedQuad/putBlockBakedQuad!");
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBakedQuad/putBlockBakedQuad!");
	}

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBakedQuad/putBlockBakedQuad!");
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBakedQuad/putBlockBakedQuad!");
	}
}
