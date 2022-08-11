package com.jozufozu.flywheel.core.model.buffering;

import java.util.function.BiFunction;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.memory.MemoryBlock;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.model.SimpleMesh;
import com.jozufozu.flywheel.core.model.TessellatedModel;
import com.jozufozu.flywheel.core.model.buffering.ModelBufferingUtil.BufferFactory;
import com.jozufozu.flywheel.core.model.buffering.ModelBufferingUtil.ResultConsumer;
import com.jozufozu.flywheel.core.model.buffering.ModelBufferingUtil.ShadeSeparatedBufferFactory;
import com.jozufozu.flywheel.core.model.buffering.ModelBufferingUtil.ShadeSeparatedResultConsumer;
import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.jozufozu.flywheel.core.virtual.VirtualEmptyModelData;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

public class BlockModelBuilder {
	private final BlockState state;
	private boolean shadeSeparated = true;
	private BlockAndTintGetter renderWorld;
	private PoseStack poseStack;
	private IModelData modelData;
	private BiFunction<RenderType, Boolean, Material> materialFunc;

	public BlockModelBuilder(BlockState state) {
		this.state = state;
	}

	public BlockModelBuilder disableShadeSeparation() {
		shadeSeparated = false;
		return this;
	}

	public BlockModelBuilder renderWorld(BlockAndTintGetter renderWorld) {
		this.renderWorld = renderWorld;
		return this;
	}

	public BlockModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public BlockModelBuilder modelData(IModelData modelData) {
		this.modelData = modelData;
		return this;
	}

	public BlockModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	@SuppressWarnings("unchecked")
	public TessellatedModel build() {
		ModelBufferingObjects objects = ModelBufferingObjects.THREAD_LOCAL.get();

		if (renderWorld == null) {
			renderWorld = VirtualEmptyBlockGetter.INSTANCE;
		}
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		if (modelData == null) {
			modelData = VirtualEmptyModelData.INSTANCE;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		ImmutableMap.Builder<Material, Mesh> meshMapBuilder = ImmutableMap.builder();

		if (shadeSeparated) {
			ShadeSeparatedBufferFactory<BufferBuilder> bufferFactory = (renderType, shaded) -> {
				BufferBuilder buffer = new BufferBuilder(64);
				buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
				return buffer;
			};
			ShadeSeparatedResultConsumer<BufferBuilder> resultConsumer = (renderType, shaded, buffer) -> {
				buffer.end();
				Material material = materialFunc.apply(renderType, shaded);
				if (material != null) {
					Pair<VertexType, MemoryBlock> pair = ModelUtil.convertBlockBuffer(buffer.popNextBuffer());
					meshMapBuilder.put(material, new SimpleMesh(pair.getFirst(), pair.getSecond(), "state=" + state.toString() + ",renderType=" + renderType.toString() + ",shaded=" + shaded));
				}
			};
			ModelBufferingUtil.bufferBlockShadeSeparated(ModelUtil.VANILLA_RENDERER, renderWorld, state, poseStack, bufferFactory, objects.shadeSeparatingBufferWrapper, objects.random, modelData, resultConsumer);
		} else {
			BufferFactory<BufferBuilder> bufferFactory = (renderType) -> {
				BufferBuilder buffer = new BufferBuilder(64);
				buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
				return buffer;
			};
			ResultConsumer<BufferBuilder> resultConsumer = (renderType, buffer) -> {
				buffer.end();
				Material material = materialFunc.apply(renderType, false);
				if (material != null) {
					Pair<VertexType, MemoryBlock> pair = ModelUtil.convertBlockBuffer(buffer.popNextBuffer());
					meshMapBuilder.put(material, new SimpleMesh(pair.getFirst(), pair.getSecond(), "state=" + state.toString() + ",renderType=" + renderType.toString()));
				}
			};
			ModelBufferingUtil.bufferBlock(ModelUtil.VANILLA_RENDERER, renderWorld, state, poseStack, bufferFactory, objects.bufferWrapper, objects.random, modelData, resultConsumer);
		}

		return new TessellatedModel(meshMapBuilder.build(), shadeSeparated);
	}
}
