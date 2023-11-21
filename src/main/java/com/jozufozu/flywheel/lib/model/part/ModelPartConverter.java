package com.jozufozu.flywheel.lib.model.part;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.vertex.VertexTypes;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class ModelPartConverter {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private ModelPartConverter() {
	}

	public static Mesh convert(ModelPart modelPart, @Nullable PoseStack poseStack, @Nullable TextureMapper textureMapper) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		VertexWriter vertexWriter = objects.vertexWriter;
		vertexWriter.setTextureMapper(textureMapper);
		modelPart.render(poseStack, vertexWriter, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		MemoryBlock data = vertexWriter.copyDataAndReset();
		return new SimpleMesh(VertexTypes.POS_TEX_NORMAL, data, "source=ModelPartConverter");
	}

	public static Mesh convert(ModelLayerLocation layer, @Nullable TextureAtlasSprite sprite, String... childPath) {
		EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
		ModelPart modelPart = entityModels.bakeLayer(layer);
		for (String pathPart : childPath) {
			modelPart = modelPart.getChild(pathPart);
		}
		TextureMapper textureMapper = sprite == null ? null : TextureMapper.toSprite(sprite);
		return convert(modelPart, null, textureMapper);
	}

	public static Mesh convert(ModelLayerLocation layer, String... childPath) {
		return convert(layer, null, childPath);
	}

	public interface TextureMapper {
		void map(Vector2f uv);

		static TextureMapper toSprite(TextureAtlasSprite sprite) {
			return uv -> uv.set(sprite.getU(uv.x * 16), sprite.getV(uv.y * 16));
		}
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final VertexWriter vertexWriter = new VertexWriter();
	}
}
