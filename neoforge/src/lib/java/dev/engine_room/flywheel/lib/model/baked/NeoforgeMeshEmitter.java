package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.api.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;

@ApiStatus.Internal
public class NeoforgeMeshEmitter extends MeshEmitter implements VertexConsumer {
	private final RenderType renderType;

	private boolean defaultAo;

	NeoforgeMeshEmitter(ByteBufferBuilderStack byteBufferBuilderStack, RenderType renderType) {
		super(byteBufferBuilderStack, renderType);
		this.renderType = renderType;
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
		Material key = blockMaterialFunction.apply(renderType, shade, ao);
		if (key != null) {
			return getBuffer(key);
		} else {
			return null;
		}
	}

	@Nullable
	private BufferBuilder getBuffer(BakedQuad quad) {
		boolean shade = quad.isShade();
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
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readAlpha) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay, readAlpha);
		}
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}
}
