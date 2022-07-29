package com.jozufozu.flywheel.core.model.buffering;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.model.SimpleMesh;
import com.jozufozu.flywheel.core.model.TessellatedModel;
import com.jozufozu.flywheel.core.model.buffering.ModelBufferingUtil.BufferFactory;
import com.jozufozu.flywheel.core.model.buffering.ModelBufferingUtil.ResultConsumer;
import com.jozufozu.flywheel.core.model.buffering.ModelBufferingUtil.ShadeSeparatedBufferFactory;
import com.jozufozu.flywheel.core.model.buffering.ModelBufferingUtil.ShadeSeparatedResultConsumer;
import com.jozufozu.flywheel.core.vertex.Formats;
import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.model.data.IModelData;

public class MultiBlockModelBuilder {
	private final Collection<StructureTemplate.StructureBlockInfo> blocks;
	private boolean shadeSeparated = true;
	private VertexFormat vertexFormat;
	private BlockAndTintGetter renderWorld;
	private PoseStack poseStack;
	private Map<BlockPos, IModelData> modelDataMap;
	private BiFunction<RenderType, Boolean, Material> materialFunc;

	public MultiBlockModelBuilder(Collection<StructureTemplate.StructureBlockInfo> blocks) {
		this.blocks = blocks;
	}

	public MultiBlockModelBuilder disableShadeSeparation() {
		shadeSeparated = false;
		return this;
	}

	public MultiBlockModelBuilder vertexFormat(VertexFormat vertexFormat) {
		this.vertexFormat = vertexFormat;
		return this;
	}

	public MultiBlockModelBuilder renderWorld(BlockAndTintGetter renderWorld) {
		this.renderWorld = renderWorld;
		return this;
	}

	public MultiBlockModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public MultiBlockModelBuilder modelDataMap(Map<BlockPos, IModelData> modelDataMap) {
		this.modelDataMap = modelDataMap;
		return this;
	}

	public MultiBlockModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	@SuppressWarnings("unchecked")
	public TessellatedModel build() {
		ModelBufferingObjects objects = ModelBufferingObjects.THREAD_LOCAL.get();

		if (vertexFormat == null) {
			vertexFormat = DefaultVertexFormat.BLOCK;
		}
		if (renderWorld == null) {
			renderWorld = VirtualEmptyBlockGetter.INSTANCE;
		}
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		if (modelDataMap == null) {
			modelDataMap = Collections.emptyMap();
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		ImmutableMap.Builder<Material, Mesh> meshMapBuilder = ImmutableMap.builder();

		if (shadeSeparated) {
			ShadeSeparatedBufferFactory<BufferBuilder> bufferFactory = (renderType, shaded) -> {
				BufferBuilder buffer = new BufferBuilder(1024);
				buffer.begin(VertexFormat.Mode.QUADS, vertexFormat);
				return buffer;
			};
			ShadeSeparatedResultConsumer<BufferBuilder> resultConsumer = (renderType, shaded, buffer) -> {
				buffer.end();
				Material material = materialFunc.apply(renderType, shaded);
				if (material != null) {
					meshMapBuilder.put(material, new SimpleMesh(ModelUtil.createVertexList(buffer), Formats.BLOCK, "renderType=" + renderType.toString() + ",shaded=" + shaded));
				}
			};
			ModelBufferingUtil.bufferMultiBlockShadeSeparated(blocks, ModelUtil.VANILLA_RENDERER, renderWorld, poseStack, bufferFactory, objects.shadeSeparatingBufferWrapper, objects.random, modelDataMap, resultConsumer);
		} else {
			BufferFactory<BufferBuilder> bufferFactory = (renderType) -> {
				BufferBuilder buffer = new BufferBuilder(1024);
				buffer.begin(VertexFormat.Mode.QUADS, vertexFormat);
				return buffer;
			};
			ResultConsumer<BufferBuilder> resultConsumer = (renderType, buffer) -> {
				buffer.end();
				Material material = materialFunc.apply(renderType, false);
				if (material != null) {
					meshMapBuilder.put(material, new SimpleMesh(ModelUtil.createVertexList(buffer), Formats.BLOCK, "renderType=" + renderType.toString()));
				}
			};
			ModelBufferingUtil.bufferMultiBlock(blocks, ModelUtil.VANILLA_RENDERER, renderWorld, poseStack, bufferFactory, objects.bufferWrapper, objects.random, modelDataMap, resultConsumer);
		}

		return new TessellatedModel(meshMapBuilder.build(), shadeSeparated);
	}
}
