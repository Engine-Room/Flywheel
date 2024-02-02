package com.jozufozu.flywheel.lib.visual.components;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;
import com.jozufozu.flywheel.lib.material.StandardMaterialShaders;
import com.jozufozu.flywheel.lib.math.MoreMath;
import com.jozufozu.flywheel.lib.model.QuadMesh;
import com.jozufozu.flywheel.lib.model.SingleMeshModel;
import com.jozufozu.flywheel.lib.visual.EntityComponent;
import com.jozufozu.flywheel.lib.visual.InstanceRecycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BoundingBoxComponent implements EntityComponent {
	private static final Material MATERIAL = SimpleMaterial.builder()
			.shaders(StandardMaterialShaders.WIREFRAME)
			.backfaceCulling(false)
			.build();
	private static final Model MODEL = new SingleMeshModel(BoundingBoxMesh.INSTANCE, MATERIAL);

	private final VisualizationContext context;
	private final Entity entity;

	private boolean showEyeBox;

	private final InstanceRecycler<TransformedInstance> recycler;

	public BoundingBoxComponent(VisualizationContext context, Entity entity) {
		this.context = context;
		this.entity = entity;
		this.showEyeBox = entity instanceof LivingEntity;

		this.recycler = new InstanceRecycler<>(this::createInstance);
	}

	private TransformedInstance createInstance() {
		TransformedInstance instance = context.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, MODEL)
				.createInstance();
		instance.setBlockLight(LightTexture.block(LightTexture.FULL_BLOCK));
		instance.setChanged();
		return instance;
	}

	public BoundingBoxComponent showEyeBox(boolean renderEyeBox) {
		this.showEyeBox = renderEyeBox;
		return this;
	}

	@Override
	public void beginFrame(VisualFrameContext context) {
		recycler.resetCount();

		var shouldRenderHitBoxes = Minecraft.getInstance()
				.getEntityRenderDispatcher()
				.shouldRenderHitBoxes();
		if (shouldRenderHitBoxes && !entity.isInvisible() && !Minecraft.getInstance()
				.showOnlyReducedInfo()) {
			double entityX = Mth.lerp(context.partialTick(), entity.xOld, entity.getX());
			double entityY = Mth.lerp(context.partialTick(), entity.yOld, entity.getY());
			double entityZ = Mth.lerp(context.partialTick(), entity.zOld, entity.getZ());

			var bbWidth = entity.getBbWidth();
			var bbHeight = entity.getBbHeight();
			var bbWidthHalf = bbWidth * 0.5;
			recycler.get()
					.loadIdentity()
					.translate(entityX - bbWidthHalf, entityY, entityZ - bbWidthHalf)
					.scale(bbWidth, bbHeight, bbWidth)
					.setChanged();

			// TODO: multipart entities and view vectors

			if (showEyeBox) {
				recycler.get()
						.loadIdentity()
						.translate(entityX - bbWidthHalf, entityY + entity.getEyeHeight() - 0.01, entityZ - bbWidthHalf)
						.scale(bbWidth, 0.02f, bbWidth)
						.setColor(255, 0, 0)
						.setChanged();
			}
		}

		recycler.discardExtra();
	}

	@Override
	public void delete() {
		recycler.delete();
	}

	private static class BoundingBoxMesh implements QuadMesh {
		private static final BoundingBoxMesh INSTANCE = new BoundingBoxMesh();
		private static final Vector4fc BOUNDING_SPHERE = new Vector4f(0.5f, 0.5f, 0.5f, MoreMath.SQRT_3_OVER_2);

		private BoundingBoxMesh() {
		}

		@Override
		public int vertexCount() {
			return 24;
		}

		@Override
		public void write(MutableVertexList vertexList) {
			// very cursed, maybe we should use vanilla ModelParts instead?
			writeVertex(vertexList, 0, 0, 0, 0, 0, 0);
			writeVertex(vertexList, 1, 1, 0, 0, 1, 0);
			writeVertex(vertexList, 2, 1, 1, 0, 1, 1);
			writeVertex(vertexList, 3, 0, 1, 0, 0, 1);

			writeVertex(vertexList, 4, 0, 0, 1, 0, 0);
			writeVertex(vertexList, 5, 0, 0, 0, 1, 0);
			writeVertex(vertexList, 6, 0, 1, 0, 1, 1);
			writeVertex(vertexList, 7, 0, 1, 1, 0, 1);

			writeVertex(vertexList, 8, 0, 1, 0, 0, 0);
			writeVertex(vertexList, 9, 1, 1, 0, 1, 0);
			writeVertex(vertexList, 10, 1, 1, 1, 1, 1);
			writeVertex(vertexList, 11, 0, 1, 1, 0, 1);

			writeVertex(vertexList, 12, 1, 0, 1, 0, 0);
			writeVertex(vertexList, 13, 0, 0, 1, 1, 0);
			writeVertex(vertexList, 14, 0, 1, 1, 1, 1);
			writeVertex(vertexList, 15, 1, 1, 1, 0, 1);

			writeVertex(vertexList, 16, 1, 0, 0, 0, 0);
			writeVertex(vertexList, 17, 1, 0, 1, 1, 0);
			writeVertex(vertexList, 18, 1, 1, 1, 1, 1);
			writeVertex(vertexList, 19, 1, 1, 0, 0, 1);

			writeVertex(vertexList, 20, 0, 0, 0, 0, 0);
			writeVertex(vertexList, 21, 1, 0, 0, 1, 0);
			writeVertex(vertexList, 22, 1, 0, 1, 1, 1);
			writeVertex(vertexList, 23, 0, 0, 1, 0, 1);
		}

		@Override
		public Vector4fc boundingSphere() {
			return BOUNDING_SPHERE;
		}

		@Override
		public void delete() {
		}

		private static void writeVertex(MutableVertexList vertexList, int i, float x, float y, float z, float u, float v) {
			vertexList.x(i, x);
			vertexList.y(i, y);
			vertexList.z(i, z);
			vertexList.r(i, 1);
			vertexList.g(i, 1);
			vertexList.b(i, 1);
			vertexList.u(i, u);
			vertexList.v(i, v);
			vertexList.light(i, LightTexture.FULL_BRIGHT);
			vertexList.normalX(i, 0);
			vertexList.normalY(i, 1);
			vertexList.normalZ(i, 0);
		}
	}
}
