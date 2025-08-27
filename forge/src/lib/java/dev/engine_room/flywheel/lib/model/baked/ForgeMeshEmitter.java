package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;

@ApiStatus.Internal
public class ForgeMeshEmitter extends MeshEmitter implements VertexConsumer {
	private final RenderType renderType;

	private boolean defaultAo;

	ForgeMeshEmitter(Supplier<BufferBuilder> bufferBuilderSupplier, RenderType renderType) {
		super(bufferBuilderSupplier);
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
		Object key = resultConsumer.createKey(renderType, shade, ao);
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
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBulkData(pose, quad, red, green, blue, light, overlay);
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay, boolean readExistingColor) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, light, overlay, readExistingColor);
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean readExistingColor) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBulkData(pose, quad, brightnesses, red, green, blue, lights, overlay, readExistingColor);
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float alpha, int[] lights, int overlay, boolean readExistingColor) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBulkData(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
		}
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public void endVertex() {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public void unsetDefaultColor() {
		throw new UnsupportedOperationException("ForgeMeshEmitter only supports putBulkData!");
	}
}
