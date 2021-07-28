package com.jozufozu.flywheel.core.model;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.model.ElementBuffer;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class BlockModel implements IModel {
	private static final MatrixStack IDENTITY = new MatrixStack();

	private final BufferBuilderReader reader;

	private final VertexFormat modelFormat;

	public BlockModel(VertexFormat modelFormat, BlockState state) {
		this(modelFormat, Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state), state);
	}

	public BlockModel(VertexFormat modelFormat, IBakedModel model, BlockState referenceState) {
		this(modelFormat, model, referenceState, IDENTITY);
	}

	public BlockModel(VertexFormat modelFormat, IBakedModel model, BlockState referenceState, MatrixStack ms) {
		this.modelFormat = modelFormat;
		reader = new BufferBuilderReader(getBufferBuilder(model, referenceState, ms));
	}

	@Override
	public VertexFormat format() {
		return modelFormat;
	}

	@Override
	public int vertexCount() {
		return reader.getVertexCount();
	}

	@Override
	public void buffer(VecBuffer buffer) {

		int vertexCount = vertexCount();

		for (int i = 0; i < vertexCount; i++) {
			buffer.putVec3(reader.getX(i), reader.getY(i), reader.getZ(i));

			buffer.putVec3(reader.getNX(i), reader.getNY(i), reader.getNZ(i));

			buffer.putVec2(reader.getU(i), reader.getV(i));
		}
	}

	@Override
	public ElementBuffer createEBO() {
		return QuadConverter.getInstance()
				.quads2Tris(vertexCount() / 4);
	}

	public static BufferBuilder getBufferBuilder(IBakedModel model, BlockState referenceState, MatrixStack ms) {
		Minecraft mc = Minecraft.getInstance();
		BlockRendererDispatcher dispatcher = mc.getBlockRenderer();
		BlockModelRenderer blockRenderer = dispatcher.getModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);

		//		BakedQuadWrapper quadReader = new BakedQuadWrapper();
		//
		//		IModelData modelData = model.getModelData(mc.world, BlockPos.ZERO.up(255), referenceState, VirtualEmptyModelData.INSTANCE);
		//		List<BakedQuad> quads = Arrays.stream(dirs)
		//				.flatMap(dir -> model.getQuads(referenceState, dir, mc.world.rand, modelData).stream())
		//				.collect(Collectors.toList());

		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		blockRenderer.renderModel(mc.level, model, referenceState, BlockPos.ZERO.above(255), ms, builder, true, mc.level.random, 42, OverlayTexture.NO_OVERLAY, VirtualEmptyModelData.INSTANCE);
		builder.end();
		return builder;
	}

	// DOWN, UP, NORTH, SOUTH, WEST, EAST, null
	private static final Direction[] dirs;

	static {
		Direction[] directions = Direction.values();

		dirs = Arrays.copyOf(directions, directions.length + 1);
	}
}
