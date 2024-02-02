package com.jozufozu.flywheel.lib.visual.components;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.QuadMesh;
import com.jozufozu.flywheel.lib.model.SingleMeshModel;
import com.jozufozu.flywheel.lib.visual.EntityComponent;
import com.jozufozu.flywheel.lib.visual.InstanceRecycler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * A component that uses instances to render the fire animation on an entity.
 */
public class FireComponent implements EntityComponent {
	private static final Material FIRE_MATERIAL = SimpleMaterial.builderOf(Materials.CHUNK_CUTOUT_UNSHADED)
			.backfaceCulling(false) // Disable backface because we want to be able to flip the model.
			.build();
	// Parameterize by the material instead of the sprite
	// because Material#sprite is a surprisingly heavy operation.
	private static final ModelCache<net.minecraft.client.resources.model.Material> FIRE_MODELS = new ModelCache<>(texture -> {
		return new SingleMeshModel(new FireMesh(texture.sprite()), FIRE_MATERIAL);
	});

	private final VisualizationContext context;
	private final Entity entity;
	private final PoseStack stack = new PoseStack();

	private final InstanceRecycler<TransformedInstance> fire0;
	private final InstanceRecycler<TransformedInstance> fire1;

	public FireComponent(VisualizationContext context, Entity entity) {
		this.context = context;
		this.entity = entity;

		fire0 = new InstanceRecycler<>(() -> createInstance(ModelBakery.FIRE_0));
		fire1 = new InstanceRecycler<>(() -> createInstance(ModelBakery.FIRE_1));
	}

	private TransformedInstance createInstance(net.minecraft.client.resources.model.Material texture) {
		TransformedInstance instance = context.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, FIRE_MODELS.get(texture))
				.createInstance();
		instance.setBlockLight(LightTexture.block(LightTexture.FULL_BLOCK));
		instance.setChanged();
		return instance;
	}

	/**
	 * Update the fire instances. You'd typically call this in your visual's
	 * {@link com.jozufozu.flywheel.api.visual.DynamicVisual#beginFrame(VisualFrameContext) beginFrame} method.
	 *
	 * @param context The frame context.
	 */
	@Override
	public void beginFrame(VisualFrameContext context) {
		fire0.resetCount();
		fire1.resetCount();

		if (entity.displayFireAnimation()) {
			setupInstances(context);
		}

		fire0.discardExtra();
		fire1.discardExtra();
	}

	private void setupInstances(VisualFrameContext context) {
		double entityX = Mth.lerp(context.partialTick(), entity.xOld, entity.getX());
		double entityY = Mth.lerp(context.partialTick(), entity.yOld, entity.getY());
		double entityZ = Mth.lerp(context.partialTick(), entity.zOld, entity.getZ());
		var renderOrigin = this.context.renderOrigin();

		final float scale = entity.getBbWidth() * 1.4F;
		final float maxHeight = entity.getBbHeight() / scale;
		float width = 1;
		float y = 0;
		float z = 0;

		stack.setIdentity();
		stack.translate(entityX - renderOrigin.getX(), entityY - renderOrigin.getY(), entityZ - renderOrigin.getZ());
		stack.scale(scale, scale, scale);
		stack.mulPose(Axis.YP.rotationDegrees(-context.camera()
				.getYRot()));
		stack.translate(0.0F, 0.0F, -0.3F + (float) ((int) maxHeight) * 0.02F);

		for (int i = 0; y < maxHeight; ++i) {
			var instance = (i % 2 == 0 ? this.fire0 : this.fire1).get()
					.setTransform(stack)
					.scaleX(width)
					.translate(0, y, z);

			if (i / 2 % 2 == 0) {
				// Vanilla flips the uv directly, but it's easier for us to flip the whole model.
				instance.scaleX(-1);
			}

			instance.setChanged();

			y += 0.45F;
			// Get narrower as we go up.
			width *= 0.9F;
			// Offset each one so they don't z-fight.
			z += 0.03F;
		}
	}

	@Override
	public void delete() {
		fire0.delete();
		fire1.delete();
	}

	private record FireMesh(TextureAtlasSprite sprite) implements QuadMesh {
		private static final Vector4fc BOUNDING_SPHERE = new Vector4f(0, 0.5f, 0, (float) (Math.sqrt(2) * 0.5));

		@Override
		public int vertexCount() {
			return 4;
		}

		@Override
		public void write(MutableVertexList vertexList) {
			float u0 = sprite.getU0();
			float v0 = sprite.getV0();
			float u1 = sprite.getU1();
			float v1 = sprite.getV1();
			writeVertex(vertexList, 0, 0.5f, 0, u1, v1);
			writeVertex(vertexList, 1, -0.5f, 0, u0, v1);
			writeVertex(vertexList, 2, -0.5f, 1.4f, u0, v0);
			writeVertex(vertexList, 3, 0.5f, 1.4f, u1, v0);
		}

		@Override
		public Vector4fc boundingSphere() {
			return BOUNDING_SPHERE;
		}

		@Override
		public void delete() {
		}

		// Magic numbers taken from:
		// net.minecraft.client.renderer.entity.EntityRenderDispatcher#fireVertex
		private static void writeVertex(MutableVertexList vertexList, int i, float x, float y, float u, float v) {
			vertexList.x(i, x);
			vertexList.y(i, y);
			vertexList.z(i, 0);
			vertexList.r(i, 1);
			vertexList.g(i, 1);
			vertexList.b(i, 1);
			vertexList.u(i, u);
			vertexList.v(i, v);
			vertexList.light(i, LightTexture.FULL_BLOCK);
			vertexList.normalX(i, 0);
			vertexList.normalY(i, 1);
			vertexList.normalZ(i, 0);
		}
	}
}
