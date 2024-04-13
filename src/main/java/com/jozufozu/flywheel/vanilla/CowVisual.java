package com.jozufozu.flywheel.vanilla;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords;
import static net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.jozufozu.flywheel.lib.visual.SimpleEntityVisual;
import com.jozufozu.flywheel.lib.visual.components.FireComponent;
import com.jozufozu.flywheel.lib.visual.components.HitboxComponent;
import com.jozufozu.flywheel.lib.visual.components.ShadowComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.LightLayer;

public class CowVisual extends SimpleEntityVisual<Cow> {
	private static final ResourceLocation COW_LOCATION = new ResourceLocation("textures/entity/cow/cow.png");

	public static final AgeableListComponent.Config COW_CONFIG = new AgeableListComponent.Config(false, 10.0F, 4.0F, 2.0F, 2.0F, 24);

	public static final Material COW_MATERIAL = SimpleMaterial.builder()
			.texture(COW_LOCATION)
			.build();

	private QuadrupedComponent cowQuadrupedComponent;

	private final PoseStack stack = new PoseStack();

	public CowVisual(VisualizationContext ctx, Cow entity) {
		super(ctx, entity);
	}

	@Override
	public void init(float partialTick) {
		cowQuadrupedComponent = new QuadrupedComponent(instancerProvider, ModelLayers.COW, COW_MATERIAL, COW_CONFIG);

		addComponent(new ShadowComponent(visualizationContext, entity).radius(0.7f));
		addComponent(new HitboxComponent(visualizationContext, entity));
		addComponent(new FireComponent(visualizationContext, entity));

		super.init(partialTick);
	}

	@Override
	public void beginFrame(Context ctx) {
		if (!isVisible(ctx.frustum())) {
			return;
		}

		super.beginFrame(ctx);

		int overlay = getOverlayCoords(entity, this.getWhiteOverlayProgress(ctx.partialTick()));
		int light = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, entity.blockPosition()), level.getBrightness(LightLayer.SKY, entity.blockPosition()));
		cowQuadrupedComponent.root.walkInstances(overlay, light, (i, o, l) -> {
			i.setOverlay(o);
			i.light(l);
			// We'll #setChanged in the
		});

		stack.setIdentity();
		TransformStack.of(stack)
				.translate(getVisualPosition(ctx.partialTick()));

		boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle()
				.shouldRiderSit());
		this.cowQuadrupedComponent.riding = shouldSit;
		this.cowQuadrupedComponent.young = entity.isBaby();

		float yBodyRot = Mth.rotLerp(ctx.partialTick(), entity.yBodyRotO, entity.yBodyRot);
		float yHeadRot = Mth.rotLerp(ctx.partialTick(), entity.yHeadRotO, entity.yHeadRot);
		float diffRot = yHeadRot - yBodyRot;
		if (shouldSit && entity.getVehicle() instanceof LivingEntity livingentity) {
			yBodyRot = Mth.rotLerp(ctx.partialTick(), livingentity.yBodyRotO, livingentity.yBodyRot);
			diffRot = yHeadRot - yBodyRot;
			float f3 = Mth.wrapDegrees(diffRot);
			if (f3 < -85.0F) {
				f3 = -85.0F;
			}

			if (f3 >= 85.0F) {
				f3 = 85.0F;
			}

			yBodyRot = yHeadRot - f3;
			if (f3 * f3 > 2500.0F) {
				yBodyRot += f3 * 0.2F;
			}

			diffRot = yHeadRot - yBodyRot;
		}

		float xRot = Mth.lerp(ctx.partialTick(), entity.xRotO, entity.getXRot());
		if (isEntityUpsideDown(entity)) {
			xRot *= -1.0F;
			diffRot *= -1.0F;
		}

		if (entity.hasPose(Pose.SLEEPING)) {
			Direction direction = entity.getBedOrientation();
			if (direction != null) {
				float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
				stack.translate((float) (-direction.getStepX()) * f4, 0.0F, (float) (-direction.getStepZ()) * f4);
			}
		}

		float bob = this.getBob(ctx.partialTick());
		this.setupRotations(stack, bob, yBodyRot, ctx.partialTick());
		stack.scale(-1.0F, -1.0F, 1.0F);
		this.scale(stack, ctx.partialTick());
		stack.translate(0.0F, -1.501F, 0.0F);
		float walkSpeed = 0.0F;
		float walkPos = 0.0F;
		if (!shouldSit && entity.isAlive()) {
			walkSpeed = entity.walkAnimation.speed(ctx.partialTick());
			walkPos = entity.walkAnimation.position(ctx.partialTick());
			if (entity.isBaby()) {
				walkPos *= 3.0F;
			}

			if (walkSpeed > 1.0F) {
				walkSpeed = 1.0F;
			}
		}

		cowQuadrupedComponent.setupAnim(walkPos, walkSpeed, entity.tickCount, diffRot, xRot);
		cowQuadrupedComponent.updateInstances(stack);
	}

	private static float sleepDirectionToRotation(Direction pFacing) {
		switch (pFacing) {
		case SOUTH:
			return 90.0F;
		case WEST:
			return 0.0F;
		case NORTH:
			return 270.0F;
		case EAST:
			return 180.0F;
		default:
			return 0.0F;
		}
	}


	/**
	 * Returns where in the swing animation the living entity is (from 0 to 1).  Args : entity, partialTickTime
	 */
	protected float getAttackAnim(float pPartialTickTime) {
		return entity.getAttackAnim(pPartialTickTime);
	}

	/**
	 * Defines what float the third param in setRotationAngles of ModelBase is
	 */
	protected float getBob(float pPartialTick) {
		return (float) entity.tickCount + pPartialTick;
	}

	protected float getFlipDegrees() {
		return 90.0F;
	}

	protected float getWhiteOverlayProgress(float pPartialTicks) {
		return 0.0F;
	}

	protected boolean isShaking() {
		return entity.isFullyFrozen();
	}

	protected void setupRotations(PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
		if (this.isShaking()) {
			pRotationYaw += (float) (Math.cos((double) entity.tickCount * 3.25D) * Math.PI * (double) 0.4F);
		}

		if (!entity.hasPose(Pose.SLEEPING)) {
			pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - pRotationYaw));
		}

		if (entity.deathTime > 0) {
			float f = ((float) entity.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
			f = Mth.sqrt(f);
			if (f > 1.0F) {
				f = 1.0F;
			}

			pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * this.getFlipDegrees()));
		} else if (entity.isAutoSpinAttack()) {
			pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - entity.getXRot()));
			pPoseStack.mulPose(Axis.YP.rotationDegrees(((float) entity.tickCount + pPartialTicks) * -75.0F));
		} else if (entity.hasPose(Pose.SLEEPING)) {
			Direction direction = entity.getBedOrientation();
			float f1 = direction != null ? sleepDirectionToRotation(direction) : pRotationYaw;
			pPoseStack.mulPose(Axis.YP.rotationDegrees(f1));
			pPoseStack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees()));
			pPoseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
		} else if (isEntityUpsideDown(entity)) {
			pPoseStack.translate(0.0F, entity.getBbHeight() + 0.1F, 0.0F);
			pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		}

	}

	protected void scale(PoseStack pPoseStack, float pPartialTickTime) {
	}

	@Override
	protected void _delete() {
		super._delete();

		cowQuadrupedComponent.delete();
	}
}
