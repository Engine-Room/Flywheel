package dev.engine_room.vanillin.visuals;

import com.mojang.math.Transformation;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.component.ShadowComponent;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockDisplayVisual extends AbstractEntityVisual<Display.BlockDisplay> implements SimpleDynamicVisual {
	private final TransformedInstance instance;
	private BlockState currentBlockState;

	private final ShadowComponent shadowComponent;

	public BlockDisplayVisual(VisualizationContext ctx, Display.BlockDisplay entity, float partialTick) {
		super(ctx, entity, partialTick);

		var blockRenderState = entity.blockRenderState();

		var state = blockRenderState != null ? blockRenderState.blockState() : Blocks.AIR.defaultBlockState();

		currentBlockState = state;

		instance = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, Models.block(state))
				.createInstance();

		shadowComponent = new ShadowComponent(ctx, entity);
	}

	@Override
	public void beginFrame(Context ctx) {
		Display.RenderState renderState = entity.renderState();
		if (renderState == null) {
			instance.handle()
					.setVisible(false);
			return;
		}
		var object = entity.blockRenderState();
		if (object == null) {
			instance.handle()
					.setVisible(false);
			return;
		}

		instance.handle()
				.setVisible(true);

		if (currentBlockState != object.blockState()) {
			currentBlockState = object.blockState();
			visualizationContext.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, Models.block(currentBlockState))
					.stealInstance(instance);
		}

		float f = entity.calculateInterpolationProgress(ctx.partialTick());

		shadowComponent.radius(renderState.shadowRadius()
				.get(f));
		shadowComponent.strength(renderState.shadowStrength()
				.get(f));
		shadowComponent.beginFrame(ctx);

		int i = renderState.brightnessOverride();
		int j = i != -1 ? i : computePackedLight(ctx.partialTick());
		Transformation transformation = renderState.transformation()
				.get(f);

		Vec3 pos = entity.position();
		var renderOrigin = renderOrigin();

		instance.setIdentityTransform()
				.translate((float) (pos.x - renderOrigin.getX()), (float) (pos.y - renderOrigin.getY()), (float) (pos.z - renderOrigin.getZ()));

		float partialTick = ctx.partialTick();
		Camera camera = ctx.camera();
		switch (renderState.billboardConstraints()) {
		case FIXED:
			instance.pose.rotateYXZ(-0.017453292F * entityYRot(entity, partialTick), ((float) Math.PI / 180F) * entityXRot(entity, partialTick), 0.0F);
			break;
		case HORIZONTAL:
			instance.pose.rotateYXZ(-0.017453292F * entityYRot(entity, partialTick), ((float) Math.PI / 180F) * cameraXRot(camera), 0.0F);
			break;
		case VERTICAL:
			instance.pose.rotateYXZ(-0.017453292F * cameraYrot(camera), ((float) Math.PI / 180F) * entityXRot(entity, partialTick), 0.0F);
			break;
		case CENTER:
			instance.pose.rotateYXZ(-0.017453292F * cameraYrot(camera), ((float) Math.PI / 180F) * cameraXRot(camera), 0.0F);
			break;
		}

		// getMatrix does a copy which is not strictly necessary here but oh well.
		instance.mul(transformation.getMatrix())
				.light(j)
				.setChanged();
	}

	private static float cameraYrot(Camera camera) {
		return camera.getYRot() - 180.0F;
	}

	private static float cameraXRot(Camera camera) {
		return -camera.getXRot();
	}

	private static float entityYRot(Entity entity, float partialTick) {
		return Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
	}

	private static float entityXRot(Entity entity, float partialTick) {
		return Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
	}

	@Override
	protected void _delete() {
		instance.delete();
	}
}
