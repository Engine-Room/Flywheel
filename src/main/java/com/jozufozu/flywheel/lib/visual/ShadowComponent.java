package com.jozufozu.flywheel.lib.visual;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.api.material.WriteMask;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.ShadowInstance;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;
import com.jozufozu.flywheel.lib.model.QuadMesh;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A component that uses instances to render an entity's shadow.
 *
 * <p>Use {@link #radius(float)} to set the radius of the shadow, in blocks.
 * <br>
 * Use {@link #strength(float)} to set the strength of the shadow.
 * <br>
 * The shadow will be cast on blocks at most {@code min(radius, 2 * strength)} blocks below the entity.</p>
 */
public class ShadowComponent {

	private final VisualizationContext context;
	private final LevelReader level;
	private final Entity entity;
	private final InstanceRecycler<ShadowInstance> instances = new InstanceRecycler<>(this::instance);
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	// Defaults taken from EntityRenderer.
	private float radius = 0;
	private float strength = 1.0F;

	public ShadowComponent(VisualizationContext context, Entity entity) {
		this.context = context;
		this.level = entity.level();
		this.entity = entity;
	}

	/**
	 * Set the radius of the shadow, in blocks, clamped to a maximum of 32.
	 *
	 * <p>Setting this to {@code <= 0} will disable the shadow.</p>
	 *
	 * @param radius The radius of the shadow, in blocks.
	 */
	public void radius(float radius) {
		this.radius = Math.min(radius, 32);
	}

	/**
	 * Set the strength of the shadow.
	 *
	 * @param strength The strength of the shadow.
	 */
	public void strength(float strength) {
		this.strength = strength;
	}

	/**
	 * Update the shadow instances. You'd typically call this in your visual's
	 * {@link com.jozufozu.flywheel.api.visual.DynamicVisual#beginFrame(VisualFrameContext) beginFrame} method.
	 *
	 * @param context The frame context.
	 */
	public void beginFrame(VisualFrameContext context) {
		instances.resetCount();

		boolean shadowsEnabled = Minecraft.getInstance().options.entityShadows()
				.get();
		if (shadowsEnabled && radius > 0 && !entity.isInvisible()) {
			setupInstances(context);
		}

		instances.discardExtra();
	}

	private void setupInstances(VisualFrameContext context) {
		double entityX = Mth.lerp(context.partialTick(), entity.xOld, entity.getX());
		double entityY = Mth.lerp(context.partialTick(), entity.yOld, entity.getY());
		double entityZ = Mth.lerp(context.partialTick(), entity.zOld, entity.getZ());
		float castDistance = Math.min(strength * 2, radius);
		int minXPos = Mth.floor(entityX - (double) radius);
		int maxXPos = Mth.floor(entityX + (double) radius);
		int minYPos = Mth.floor(entityY - (double) castDistance);
		int maxYPos = Mth.floor(entityY);
		int minZPos = Mth.floor(entityZ - (double) radius);
		int maxZPos = Mth.floor(entityZ + (double) radius);

		for (int z = minZPos; z <= maxZPos; ++z) {
			for (int x = minXPos; x <= maxXPos; ++x) {
				pos.set(x, 0, z);
				ChunkAccess chunkaccess = level.getChunk(pos);

				for (int y = minYPos; y <= maxYPos; ++y) {
					pos.setY(y);
					float strengthGivenYFalloff = strength - (float) (entityY - pos.getY()) * 0.5F;
					maybeSetupShadowInstance(chunkaccess, (float) entityX, (float) entityZ, strengthGivenYFalloff);
				}
			}
		}
	}

	private void maybeSetupShadowInstance(ChunkAccess pChunk, float entityX, float entityZ, float strength) {
		// TODO: cache this?
		var maxLocalRawBrightness = level.getMaxLocalRawBrightness(pos);
		if (maxLocalRawBrightness <= 3) {
			// Too dark to render.
			return;
		}
		float blockBrightness = LightTexture.getBrightness(level.dimensionType(), maxLocalRawBrightness);
		float alpha = strength * 0.5F * blockBrightness;
		if (!(alpha >= 0.0F)) {
			// Too far away/too weak to render.
			return;
		}
		if (alpha > 1.0F) {
			alpha = 1.0F;
		}

		// Grab the AABB for the block below the current position.
		pos.setY(pos.getY() - 1);
		var aabb = getAabbForPos(pChunk, pos);
		if (aabb == null) {
			// No aabb means the block shouldn't receive a shadow.
			return;
		}

		var renderOrigin = context.renderOrigin();
		int x = pos.getX() - renderOrigin.getX();
		int y = pos.getY() - renderOrigin.getY() + 1; // +1 since we moved the pos down.
		int z = pos.getZ() - renderOrigin.getZ();

		double minX = x + aabb.minX;
		double minY = y + aabb.minY;
		double minZ = z + aabb.minZ;
		double maxX = x + aabb.maxX;
		double maxZ = z + aabb.maxZ;

		var instance = instances.get();
		instance.x = (float) minX;
		instance.y = (float) minY;
		instance.z = (float) minZ;
		instance.entityX = entityX;
		instance.entityZ = entityZ;
		instance.sizeX = (float) (maxX - minX);
		instance.sizeZ = (float) (maxZ - minZ);
		instance.alpha = alpha;
		instance.radius = this.radius;
		instance.setChanged();
	}

	@Nullable
	private AABB getAabbForPos(ChunkAccess pChunk, BlockPos pos) {
		BlockState blockstate = pChunk.getBlockState(pos);
		if (blockstate.getRenderShape() == RenderShape.INVISIBLE) {
			return null;
		}
		if (!blockstate.isCollisionShapeFullBlock(pChunk, pos)) {
			return null;
		}
		VoxelShape voxelshape = blockstate.getShape(pChunk, pos);
		if (voxelshape.isEmpty()) {
			return null;
		}
		return voxelshape.bounds();
	}

	private ShadowInstance instance() {
		return context.instancerProvider()
				.instancer(InstanceTypes.SHADOW, ShadowModel.INSTANCE, RenderStage.AFTER_ENTITIES)
				.createInstance();
	}

	public void delete() {
		instances.delete();
	}

	private static class ShadowModel implements Model {
		public static final ShadowModel INSTANCE = new ShadowModel();
		public static final Material MATERIAL = SimpleMaterial.builder()
				.transparency(Transparency.TRANSLUCENT)
				.writeMask(WriteMask.COLOR)
				.polygonOffset(true) // vanilla shadows use "view offset" but this seems to work fine
				.texture(new ResourceLocation("minecraft", "textures/misc/shadow.png"))
				.build();
		private static final Vector4fc BOUNDING_SPHERE = new Vector4f(0.5f, 0, 0.5f, (float) (Math.sqrt(2) * 0.5));
		private static final ImmutableMap<Material, Mesh> meshes = ImmutableMap.of(MATERIAL, ShadowMesh.INSTANCE);

		private ShadowModel() {
		}

		@Override
		public Map<Material, Mesh> meshes() {
			return meshes;
		}

		@Override
		public Vector4fc boundingSphere() {
			return BOUNDING_SPHERE;
		}

		@Override
		public int vertexCount() {
			return ShadowMesh.INSTANCE.vertexCount();
		}

		@Override
		public void delete() {

		}

		/**
		 * A single quad extending from the origin to (1, 0, 1).
		 * <br>
		 * To be scaled and translated to the correct position and size.
		 */
		private static class ShadowMesh implements QuadMesh {
			public static final ShadowMesh INSTANCE = new ShadowMesh();

			private ShadowMesh() {
			}

			@Override
			public int vertexCount() {
				return 4;
			}

			@Override
			public void write(MutableVertexList vertexList) {
				writeVertex(vertexList, 0, 0, 0);
				writeVertex(vertexList, 1, 0, 1);
				writeVertex(vertexList, 2, 1, 1);
				writeVertex(vertexList, 3, 1, 0);
			}

			@Override
			public Vector4fc boundingSphere() {
				return BOUNDING_SPHERE;
			}

			@Override
			public void delete() {

			}

			// Magic numbers taken from:
			// net.minecraft.client.renderer.entity.EntityRenderDispatcher#shadowVertex
			private static void writeVertex(MutableVertexList vertexList, int i, float x, float z) {
				vertexList.x(i, x);
				vertexList.y(i, 0);
				vertexList.z(i, z);
				vertexList.r(i, 1);
				vertexList.g(i, 1);
				vertexList.b(i, 1);
				vertexList.u(i, 0);
				vertexList.v(i, 0);
				vertexList.light(i, 15728880);
				vertexList.normalX(i, 0);
				vertexList.normalY(i, 1);
				vertexList.normalZ(i, 0);

			}
		}
	}

}
