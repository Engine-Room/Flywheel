package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;

class ModelBuilderResultConsumer {
	private final BlockMaterialFunction materialFunc;
	private final ImmutableList.Builder<Model.ConfiguredMesh> meshes = ImmutableList.builder();

	ModelBuilderResultConsumer(BlockMaterialFunction materialFunc) {
		this.materialFunc = materialFunc;
	}

	@Nullable
	public Material createKey(RenderType renderType, boolean shade, boolean ambientOcclusion) {
		return materialFunc.apply(renderType, shade, ambientOcclusion);
	}

	public void accept(Material material, BufferBuilder.RenderedBuffer data) {
		Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=ModelBuilder" + ",material=" + material);
		meshes.add(new Model.ConfiguredMesh(material, mesh));
	}

	public SimpleModel build() {
		return new SimpleModel(meshes.build());
	}
}
