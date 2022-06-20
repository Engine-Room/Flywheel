package com.jozufozu.flywheel.vanilla;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.InstancerManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.core.BasicModelSupplier;
import com.jozufozu.flywheel.core.Models;
import com.jozufozu.flywheel.core.hardcoded.ModelPart;
import com.jozufozu.flywheel.core.material.MaterialShaders;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.structs.StructTypes;
import com.jozufozu.flywheel.core.structs.model.TransformedPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartInstance<T extends AbstractMinecart> extends EntityInstance<T> implements DynamicInstance, TickableInstance {

	private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
	private static final BasicModelSupplier MODEL = new BasicModelSupplier(MinecartInstance::getBodyModel, new Material(RenderType.entitySolid(MINECART_LOCATION), MaterialShaders.SHADED_VERTEX, MaterialShaders.DEFAULT_FRAGMENT));

	private final PoseStack stack = new PoseStack();

	private final TransformedPart body;
	private TransformedPart contents;
	private BlockState blockState;
	private boolean active;

	public MinecartInstance(InstancerManager instancerManager, T entity) {
		super(instancerManager, entity);

		body = getBody();
		blockState = entity.getDisplayBlockState();
		contents = getContents();
	}

	@Override
	public boolean decreaseFramerateWithDistance() {
		return false;
	}

	@Override
	public void tick() {
		BlockState displayBlockState = entity.getDisplayBlockState();

		if (displayBlockState != blockState) {
			blockState = displayBlockState;
			contents.delete();
			contents = getContents();
			if (contents != null) {
				relight(getWorldPosition(), contents);
			}
		}
	}

	@Override
	public void beginFrame() {
		// TODO: add proper way to temporarily disable rendering a specific instance
		if (!active) {
			return;
		}

		TransformStack tstack = TransformStack.cast(stack);
		stack.setIdentity();
		float pt = AnimationTickHolder.getPartialTicks();

		Vec3i originCoordinate = instancerManager.getOriginCoordinate();
		tstack.translate(
				Mth.lerp(pt, entity.xOld, entity.getX()) - originCoordinate.getX(),
				Mth.lerp(pt, entity.yOld, entity.getY()) - originCoordinate.getY(),
				Mth.lerp(pt, entity.zOld, entity.getZ()) - originCoordinate.getZ());

		float yaw = Mth.lerp(pt, entity.yRotO, entity.getYRot());

		long i = (long)entity.getId() * 493286711L;
		i = i * i * 4392167121L + i * 98761L;
		float f = (((float)(i >> 16 & 7L) + 0.5F) / 8 - 0.5F) * 0.004F;
		float f1 = (((float)(i >> 20 & 7L) + 0.5F) / 8 - 0.5F) * 0.004F;
		float f2 = (((float)(i >> 24 & 7L) + 0.5F) / 8 - 0.5F) * 0.004F;
		tstack.translate(f, f1, f2);
		tstack.nudge(entity.getId());
		double d0 = Mth.lerp(pt, entity.xOld, entity.getX());
		double d1 = Mth.lerp(pt, entity.yOld, entity.getY());
		double d2 = Mth.lerp(pt, entity.zOld, entity.getZ());
		Vec3 vector3d = entity.getPos(d0, d1, d2);
		float f3 = Mth.lerp(pt, entity.xRotO, entity.getXRot());
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
		float f5 = (float)entity.getHurtTime() - pt;
		float f6 = entity.getDamage() - pt;
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
		if (contents == null)
			relight(getWorldPosition(), body);
		else
			relight(getWorldPosition(), body, contents);
	}

	@Override
	public void remove() {
		body.delete();
		if (contents != null) contents.delete();
	}

	private TransformedPart getContents() {
		RenderShape shape = blockState.getRenderShape();

		if (shape == RenderShape.ENTITYBLOCK_ANIMATED) {
			body.setEmptyTransform();
			active = false;
			return null;
		}
		active = true;

		if (shape == RenderShape.INVISIBLE)
			return null;

        return instancerManager.factory(StructTypes.TRANSFORMED)
				.model(Models.block(blockState))
				.createInstance();
	}

	private TransformedPart getBody() {
		return instancerManager.factory(StructTypes.TRANSFORMED)
				.model(MODEL)
				.createInstance();
	}

	@NotNull
	private static Mesh getBodyModel() {
		int y = -3;
		return ModelPart.builder("minecart", 64, 32)
				.cuboid().invertYZ().start(-10, -8, -y).size(20, 16, 2).textureOffset(0, 10).rotateZ((float) Math.PI).rotateX(((float)Math.PI / 2F)).endCuboid()
				.cuboid().invertYZ().start(-8, y, -10).size(16, 8, 2).rotateY(((float)Math.PI * 1.5F)).endCuboid()
				.cuboid().invertYZ().start(-8, y, -10).size(16, 8, 2).rotateY(((float)Math.PI / 2F)).endCuboid()
				.cuboid().invertYZ().start(-8, y, -8).size(16, 8, 2).rotateY((float)Math.PI).endCuboid()
				.cuboid().invertYZ().start(-8, y, -8).size(16, 8, 2).endCuboid()
				.build();
	}

	public static boolean shouldSkipRender(AbstractMinecart minecart) {
		return minecart.getDisplayBlockState().getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED;
	}
}
