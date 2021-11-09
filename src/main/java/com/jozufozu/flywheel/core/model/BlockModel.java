package com.jozufozu.flywheel.core.model;

import java.util.Arrays;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

/**
 * A model of a single block.
 */
public class BlockModel implements IModel {
	private static final PoseStack IDENTITY = new PoseStack();

	private final BufferBuilderReader reader;

	private final String name;

	public BlockModel(BlockState state) {
		this(Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state), state);
	}

	public BlockModel(BakedModel model, BlockState referenceState) {
		this(model, referenceState, IDENTITY);
	}

	public BlockModel(BakedModel model, BlockState referenceState, PoseStack ms) {
		reader = new BufferBuilderReader(getBufferBuilder(model, referenceState, ms));
		name = referenceState.toString();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public VertexFormat format() {
		return Formats.UNLIT_MODEL;
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

	public static BufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack ms) {
		Minecraft mc = Minecraft.getInstance();
		BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
		ModelBlockRenderer blockRenderer = dispatcher.getModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);

		//		BakedQuadWrapper quadReader = new BakedQuadWrapper();
		//
		//		IModelData modelData = model.getModelData(mc.world, BlockPos.ZERO.up(255), referenceState, VirtualEmptyModelData.INSTANCE);
		//		List<BakedQuad> quads = Arrays.stream(dirs)
		//				.flatMap(dir -> model.getQuads(referenceState, dir, mc.world.rand, modelData).stream())
		//				.collect(Collectors.toList());

		builder.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		blockRenderer.tesselateBlock(mc.level, model, referenceState, BlockPos.ZERO.above(255), ms, builder, true, mc.level.random, 42, OverlayTexture.NO_OVERLAY, VirtualEmptyModelData.INSTANCE);
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
