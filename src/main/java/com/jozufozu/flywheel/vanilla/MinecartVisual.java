package com.jozufozu.flywheel.vanilla;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.model.SimpleLazyModel;
import com.jozufozu.flywheel.lib.model.part.ModelPartBuilder;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.jozufozu.flywheel.lib.visual.AbstractEntityVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartVisual<T extends AbstractMinecart> extends AbstractEntityVisual<T> implements TickableVisual, DynamicVisual {
	private static final SimpleLazyModel BODY_MODEL = new SimpleLazyModel(MinecartVisual::createBodyMesh, Materials.MINECART);

	private final PoseStack stack = new PoseStack();

	private TransformedInstance body;
	private TransformedInstance contents;
	private BlockState blockState;
	private boolean active;

	public MinecartVisual(VisualizationContext ctx, T entity) {
		super(ctx, entity);
	}

	@Override
	public void init(float partialTick) {
		body = createBodyInstance();
		blockState = entity.getDisplayBlockState();
		contents = createContentsInstance();

		updatePosition(partialTick);

		super.init(partialTick);
	}

	@Override
	public void tick(VisualTickContext c) {
		BlockState displayBlockState = entity.getDisplayBlockState();

		if (displayBlockState != blockState) {
			blockState = displayBlockState;
			contents.delete();
			contents = createContentsInstance();
			if (contents != null) {
				relight(entity.blockPosition(), contents);
			}
		}
	}

	@Override
	public void beginFrame(VisualFrameContext context) {
		if (isVisible(context.frustum())) {
			return;
		}

		// TODO: add proper way to temporarily disable rendering a specific instance
		if (!active) {
			return;
		}

		updatePosition(context.partialTick());
	}

	private void updatePosition(float partialTick) {
		TransformStack tstack = TransformStack.cast(stack);
		stack.setIdentity();

		tstack.translate(Mth.lerp(partialTick, entity.xOld, entity.getX()) - renderOrigin.getX(), Mth.lerp(partialTick, entity.yOld, entity.getY()) - renderOrigin.getY(), Mth.lerp(partialTick, entity.zOld, entity.getZ()) - renderOrigin.getZ());

		float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());

		long i = (long) entity.getId() * 493286711L;
		i = i * i * 4392167121L + i * 98761L;
		float f = (((float)(i >> 16 & 7L) + 0.5F) / 8 - 0.5F) * 0.004F;
		float f1 = (((float)(i >> 20 & 7L) + 0.5F) / 8 - 0.5F) * 0.004F;
		float f2 = (((float)(i >> 24 & 7L) + 0.5F) / 8 - 0.5F) * 0.004F;
		tstack.translate(f, f1, f2);
		tstack.nudge(entity.getId());
		double d0 = Mth.lerp(partialTick, entity.xOld, entity.getX());
		double d1 = Mth.lerp(partialTick, entity.yOld, entity.getY());
		double d2 = Mth.lerp(partialTick, entity.zOld, entity.getZ());
		Vec3 vector3d = entity.getPos(d0, d1, d2);
		float f3 = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
		if (vector3d != null) {
			Vec3 vector3d1 = entity.getPosOffs(d0, d1, d2, 0.3F);
			Vec3 vector3d2 = entity.getPosOffs(d0, d1, d2, -0.3F);
			if (vector3d1 == null) {
				vector3d1 = vector3d;
			}

			if (vector3d2 == null) {
				vector3d2 = vector3d;
			}

			tstack.translate(vector3d.x - d0, (vector3d1.y + vector3d2.y) / 2.0D - d1, vector3d.z - d2);
			Vec3 vector3d3 = vector3d2.add(-vector3d1.x, -vector3d1.y, -vector3d1.z);
			if (vector3d3.length() != 0.0D) {
				vector3d3 = vector3d3.normalize();
				yaw = (float)(Math.atan2(vector3d3.z, vector3d3.x) * 180.0D / Math.PI);
				f3 = (float)(Math.atan(vector3d3.y) * 73.0D);
			}
		}

		tstack.translate(0.0D, 0.375D, 0.0D);
		tstack.multiply(Vector3f.YP.rotationDegrees(180 - yaw));
		tstack.multiply(Vector3f.ZP.rotationDegrees(-f3));
		float f5 = (float)entity.getHurtTime() - partialTick;
		float f6 = entity.getDamage() - partialTick;
		if (f6 < 0) {
			f6 = 0;
		}

		if (f5 > 0) {
			tstack.multiply(Vector3f.XP.rotationDegrees(Mth.sin(f5) * f5 * f6 / 10 * (float)entity.getHurtDir()));
		}

		int j = entity.getDisplayOffset();
		if (contents != null) {
			tstack.pushPose();
			tstack.scale(0.75F);
			tstack.translate(-0.5D, (float)(j - 8) / 16, 0.5D);
			tstack.multiply(Vector3f.YP.rotationDegrees(90));
			contents.setTransform(stack);
			tstack.popPose();
		}

		body.setTransform(stack);
	}

	@Override
	public void updateLight() {
		if (contents == null) {
			relight(entity.blockPosition(), body);
		} else {
			relight(entity.blockPosition(), body, contents);
		}
	}

	@Override
	protected void _delete() {
		body.delete();
		if (contents != null) {
			contents.delete();
		}
	}

	private TransformedInstance createBodyInstance() {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, BODY_MODEL, RenderStage.AFTER_ENTITIES)
				.createInstance();
	}

	private TransformedInstance createContentsInstance() {
		RenderShape shape = blockState.getRenderShape();

		if (shape == RenderShape.ENTITYBLOCK_ANIMATED) {
			body.setEmptyTransform();
			active = false;
			return null;
		}
		active = true;

		if (shape == RenderShape.INVISIBLE) {
			return null;
		}

        return instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.block(blockState), RenderStage.AFTER_ENTITIES)
				.createInstance();
	}

	@NotNull
	private static Mesh createBodyMesh() {
		return new ModelPartBuilder("minecart", 64, 32)
				.cuboid().invertYZ().start(-10, -8, 3).size(20, 16, 2).textureOffset(0, 10).rotateZ((float) Math.PI).rotateX(((float)Math.PI / 2F)).endCuboid()
				.cuboid().invertYZ().start(-8, -3, -10).size(16, 8, 2).rotateY(((float)Math.PI * 1.5F)).endCuboid()
				.cuboid().invertYZ().start(-8, -3, -10).size(16, 8, 2).rotateY(((float)Math.PI / 2F)).endCuboid()
				.cuboid().invertYZ().start(-8, -3, -8).size(16, 8, 2).rotateY((float)Math.PI).endCuboid()
				.cuboid().invertYZ().start(-8, -3, -8).size(16, 8, 2).endCuboid()
				.build();
	}

	public static boolean shouldSkipRender(AbstractMinecart minecart) {
		return minecart.getDisplayBlockState().getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED;
	}
}
