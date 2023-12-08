package com.jozufozu.flywheel.vanilla;

import java.util.List;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.SingleMeshModel;
import com.jozufozu.flywheel.lib.model.part.ModelPartConverter;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class ShulkerBoxVisual extends AbstractBlockEntityVisual<ShulkerBoxBlockEntity> implements DynamicVisual {
	private static final ModelCache<Material> BASE_MODELS = new ModelCache<>(texture -> {
		return new SingleMeshModel(ModelPartConverter.convert(ModelLayers.SHULKER, texture.sprite(), "base"), Materials.SHULKER);
	});
	private static final ModelCache<Material> LID_MODELS = new ModelCache<>(texture -> {
		return new SingleMeshModel(ModelPartConverter.convert(ModelLayers.SHULKER, texture.sprite(), "lid"), Materials.SHULKER);
	});

	private TransformedInstance base;
	private TransformedInstance lid;

	private final PoseStack stack = new PoseStack();

	private float lastProgress = Float.NaN;

	public ShulkerBoxVisual(VisualizationContext ctx, ShulkerBoxBlockEntity blockEntity) {
		super(ctx, blockEntity);
	}

	@Override
	public void init(float partialTick) {
		DyeColor color = blockEntity.getColor();
		Material texture;
		if (color == null) {
			texture = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION;
		} else {
			texture = Sheets.SHULKER_TEXTURE_LOCATION.get(color.getId());
		}

		var rotation = getDirection().getRotation();

		stack.setIdentity();
		TransformStack.of(stack)
				.translate(getVisualPosition())
				.translate(0.5)
				.scale(0.9995f)
				.rotate(rotation)
				.scale(1, -1, -1)
				.translateY(-1);

		base = createBaseInstance(texture).setTransform(stack);
		lid = createLidInstance(texture).setTransform(stack);

		super.init(partialTick);
	}

	private TransformedInstance createBaseInstance(Material texture) {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, BASE_MODELS.get(texture), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private TransformedInstance createLidInstance(Material texture) {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, LID_MODELS.get(texture), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private Direction getDirection() {
		if (blockState.getBlock() instanceof ShulkerBoxBlock) {
			return blockState.getValue(ShulkerBoxBlock.FACING);
		}

		return Direction.UP;
	}

	@Override
	public void beginFrame(VisualFrameContext context) {
		if (doDistanceLimitThisFrame(context) || !isVisible(context.frustum())) {
			return;
		}

		float progress = blockEntity.getProgress(context.partialTick());
		if (progress == lastProgress) {
			return;
		}
		lastProgress = progress;

		Quaternionf spin = Axis.YP.rotationDegrees(270.0f * progress);

		TransformStack.of(stack)
				.pushPose()
				.translateY(-progress * 0.5f)
				.rotate(spin);

		lid.setTransform(stack)
				.setChanged();

		stack.popPose();
	}

	@Override
	public void updateLight() {
		relight(pos, base, lid);
	}

	@Override
	public List<Instance> getCrumblingInstances() {
		return List.of(base, lid);
	}

	@Override
	protected void _delete() {
		base.delete();
		lid.delete();
	}
}
