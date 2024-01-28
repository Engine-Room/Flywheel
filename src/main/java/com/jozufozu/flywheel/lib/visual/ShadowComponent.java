package com.jozufozu.flywheel.lib.visual;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.api.material.WriteMask;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.ShadowInstance;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;
import com.jozufozu.flywheel.lib.model.QuadMesh;
import com.jozufozu.flywheel.lib.model.SingleMeshModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
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
	private static final Material SHADOW_MATERIAL = SimpleMaterial.builder()
			.texture(new ResourceLocation("textures/misc/shadow.png"))
			.mipmap(false)
			.polygonOffset(true) // vanilla shadows use "view offset" but this seems to work fine
			.transparency(Transparency.TRANSLUCENT)
			.writeMask(WriteMask.COLOR)
			.build();
	private static final Model SHADOW_MODEL = new SingleMeshModel(ShadowMesh.INSTANCE, SHADOW_MATERIAL);

	private final VisualizationContext context;
	private final Entity entity;
	private final Level level;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private final InstanceRecycler<ShadowInstance> instances = new InstanceRecycler<>(this::createInstance);

	// Defaults taken from EntityRenderer.
	private float radius = 0;
	private float strength = 1.0F;

	public ShadowComponent(VisualizationContext context, Entity entity) {
		this.context = context;
		this.entity = entity;
		this.level = entity.level();
	}

	private ShadowInstance createInstance() {
		return context.instancerProvider()
				.instancer(InstanceTypes.SHADOW, SHADOW_MODEL, RenderStage.AFTER_ENTITIES)
				.createInstance();
	}

	public float radius() {
		return radius;
	}

	public float strength() {
		return strength;
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
				ChunkAccess chunk = level.getChunk(pos);

				for (int y = minYPos; y <= maxYPos; ++y) {
					pos.setY(y);
					float strengthGivenYFalloff = strength - (float) (entityY - pos.getY()) * 0.5F;
					setupInstance(chunk, pos, (float) entityX, (float) entityZ, strengthGivenYFalloff);
				}
			}
		}
	}

	private void setupInstance(ChunkAccess chunk, MutableBlockPos pos, float entityX, float entityZ, float strength) {
		// TODO: cache this?
		var maxLocalRawBrightness = level.getMaxLocalRawBrightness(pos);
		if (maxLocalRawBrightness <= 3) {
			// Too dark to render.
			return;
		}
		float blockBrightness = LightTexture.getBrightness(level.dimensionType(), maxLocalRawBrightness);
		float alpha = strength * 0.5F * blockBrightness;
		if (alpha < 0.0F) {
			// Too far away/too weak to render.
			return;
		}
		if (alpha > 1.0F) {
			alpha = 1.0F;
		}

		// Grab the AABB for the block below the current position.
		pos.setY(pos.getY() - 1);
		var shape = getShapeAt(chunk, pos);
		if (shape == null) {
			// No shape means the block shouldn't receive a shadow.
			return;
		}

		var renderOrigin = context.renderOrigin();
		int x = pos.getX() - renderOrigin.getX();
		int y = pos.getY() - renderOrigin.getY() + 1; // +1 since we moved the pos down.
		int z = pos.getZ() - renderOrigin.getZ();

		double minX = x + shape.min(Axis.X);
		double minY = y + shape.min(Axis.Y);
		double minZ = z + shape.min(Axis.Z);
		double maxX = x + shape.max(Axis.X);
		double maxZ = z + shape.max(Axis.Z);

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
	private VoxelShape getShapeAt(ChunkAccess chunk, BlockPos pos) {
		BlockState state = chunk.getBlockState(pos);
		if (state.getRenderShape() == RenderShape.INVISIBLE) {
			return null;
		}
		if (!state.isCollisionShapeFullBlock(chunk, pos)) {
			return null;
		}
		VoxelShape shape = state.getShape(chunk, pos);
		if (shape.isEmpty()) {
			return null;
		}
		return shape;
	}

	public void delete() {
		instances.delete();
	}

	/**
	 * A single quad extending from the origin to (1, 0, 1).
	 * <br>
	 * To be scaled and translated to the correct position and size.
	 */
	private static class ShadowMesh implements QuadMesh {
		private static final Vector4fc BOUNDING_SPHERE = new Vector4f(0.5f, 0, 0.5f, (float) (Math.sqrt(2) * 0.5));
		private static final ShadowMesh INSTANCE = new ShadowMesh();

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
			vertexList.light(i, LightTexture.FULL_BRIGHT);
			vertexList.normalX(i, 0);
			vertexList.normalY(i, 1);
			vertexList.normalZ(i, 0);
		}
	}
}
