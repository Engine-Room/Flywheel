package dev.engine_room.vanillin.visuals;

import com.mojang.math.Transformation;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.component.ShadowComponent;
import dev.engine_room.vanillin.item.ItemModels;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class ItemDisplayVisual extends AbstractEntityVisual<Display.ItemDisplay> implements SimpleDynamicVisual {
	private final TransformedInstance instance;

	private ItemStack currentStack;

	private final ShadowComponent shadowComponent;

	public ItemDisplayVisual(VisualizationContext ctx, Display.ItemDisplay entity, float partialTick) {
		super(ctx, entity, partialTick);

		var itemRenderState = entity.itemRenderState();

		if (itemRenderState == null) {
			currentStack = ItemStack.EMPTY;
			instance = ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, Models.block(Blocks.AIR.defaultBlockState()))
					.createInstance();
		} else {
			currentStack = itemRenderState.itemStack()
					.copy();
			instance = ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, ItemModels.get(level, currentStack, itemRenderState.itemTransform()))
					.createInstance();
		}

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
		var object = entity.itemRenderState();
		if (object == null) {
			instance.handle()
					.setVisible(false);
			return;
		}

		instance.handle()
				.setVisible(true);

		var itemStack = object.itemStack();

		if (!ItemStack.matches(itemStack, currentStack)) {
			currentStack = itemStack.copy();
			visualizationContext.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, ItemModels.get(level, currentStack, object.itemTransform()))
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
				.rotateY(Mth.PI)
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

	public static boolean shouldVisualize(Display.ItemDisplay itemDisplay) {
		var state = itemDisplay.itemRenderState();
		return state != null && ItemModels.isSupported(state.itemStack());
	}
}
