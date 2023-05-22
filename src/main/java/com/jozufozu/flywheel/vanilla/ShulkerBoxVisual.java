package com.jozufozu.flywheel.vanilla;

import java.util.List;
import java.util.function.Function;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.model.SimpleLazyModel;
import com.jozufozu.flywheel.lib.modelpart.ModelPart;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.jozufozu.flywheel.lib.util.AnimationTickHolder;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.Util;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class ShulkerBoxVisual extends AbstractBlockEntityVisual<ShulkerBoxBlockEntity> implements DynamicVisual {
	private static final Function<TextureAtlasSprite, SimpleLazyModel> BODY_MODEL_FUNC = Util.memoize(it -> new SimpleLazyModel(() -> createBodyMesh(it), Materials.SHULKER));
	private static final Function<TextureAtlasSprite, SimpleLazyModel> LID_MODEL_FUNC = Util.memoize(it -> new SimpleLazyModel(() -> createLidMesh(it), Materials.SHULKER));

	private TextureAtlasSprite texture;

	private TransformedInstance body;
	private TransformedInstance lid;
	private final PoseStack stack = new PoseStack();

	private float lastProgress = Float.NaN;

	public ShulkerBoxVisual(VisualizationContext ctx, ShulkerBoxBlockEntity blockEntity) {
		super(ctx, blockEntity);
	}

	@Override
	public void init() {
		DyeColor color = blockEntity.getColor();
		if (color == null) {
			texture = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.sprite();
		} else {
			texture = Sheets.SHULKER_TEXTURE_LOCATION.get(color.getId())
					.sprite();
		}
		Quaternion rotation = getDirection().getRotation();

		TransformStack tstack = TransformStack.cast(stack);

		tstack.translate(getVisualPosition())
				.scale(0.9995f)
				.translateAll(0.00025)
				.centre()
				.multiply(rotation)
				.unCentre();

		body = createBodyInstance().setTransform(stack);

		tstack.translateY(0.25);

		lid = createLidInstance().setTransform(stack);

		super.init();
	}

	private Direction getDirection() {
		if (blockState.getBlock() instanceof ShulkerBoxBlock) {
			return blockState.getValue(ShulkerBoxBlock.FACING);
		}

		return Direction.UP;
	}

	@Override
	public void beginFrame(VisualFrameContext context) {
		if (doDistanceLimitThisFrame(context) || !visible(context.frustum())) {
			return;
		}
		float progress = blockEntity.getProgress(AnimationTickHolder.getPartialTicks());

		if (progress == lastProgress) {
			return;
		}
		lastProgress = progress;

		Quaternion spin = Vector3f.YP.rotationDegrees(270.0F * progress);

		TransformStack.cast(stack)
				.pushPose()
				.centre()
				.multiply(spin)
				.unCentre()
				.translateY(progress * 0.5f);

		lid.setTransform(stack);

		stack.popPose();
	}

	@Override
	public List<Instance> getCrumblingInstances() {
		return List.of(body, lid);
	}

	@Override
	protected void _delete() {
		body.delete();
		lid.delete();
	}

	@Override
	public void updateLight() {
		relight(pos, body, lid);
	}

	private TransformedInstance createBodyInstance() {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, BODY_MODEL_FUNC.apply(texture), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private TransformedInstance createLidInstance() {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, LID_MODEL_FUNC.apply(texture), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private static ModelPart createBodyMesh(TextureAtlasSprite texture) {
		return ModelPart.builder("shulker_base", 64, 64)
				.sprite(texture)
				.cuboid()
				.textureOffset(0, 28)
				.size(16, 8, 16)
				.invertYZ()
				.endCuboid()
				.build();
	}

	private static ModelPart createLidMesh(TextureAtlasSprite texture) {
		return ModelPart.builder("shulker_lid", 64, 64)
				.sprite(texture)
				.cuboid()
				.size(16, 12, 16)
				.invertYZ()
				.endCuboid()
				.build();
	}
}
