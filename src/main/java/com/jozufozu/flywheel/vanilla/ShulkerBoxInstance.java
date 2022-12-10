package com.jozufozu.flywheel.vanilla;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.hardcoded.ModelPart;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class ShulkerBoxInstance extends BlockEntityInstance<ShulkerBoxBlockEntity> implements DynamicInstance {

	private final TextureAtlasSprite texture;

	private final ModelData base;
	private final ModelData lid;
	private final PoseStack stack = new PoseStack();

	private float lastProgress = Float.NaN;

	public ShulkerBoxInstance(MaterialManager materialManager, ShulkerBoxBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		DyeColor color = blockEntity.getColor();
		if (color == null) {
			texture = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.sprite();
		} else {
			texture = Sheets.SHULKER_TEXTURE_LOCATION.get(color.getId()).sprite();
		}
		Quaternionf rotation = getDirection().getRotation();

		TransformStack tstack = TransformStack.cast(stack);

		tstack.translate(getInstancePosition())
				.scale(0.9995f)
				.translateAll(0.00025)
				.centre()
				.multiply(rotation)
				.unCentre();

		base = makeBaseInstance().setTransform(stack);

		tstack.translateY(0.25);

		lid = makeLidInstance().setTransform(stack);
	}

	@Override
	public void beginFrame() {
		float progress = blockEntity.getProgress(AnimationTickHolder.getPartialTicks());

		if (progress == lastProgress) return;
		lastProgress = progress;

		Quaternionf spin = Axis.YP.rotationDegrees(270.0F * progress);

		TransformStack tstack = TransformStack.cast(stack);

		tstack.pushPose()
				.centre()
				.multiply(spin)
				.unCentre()
				.translateY(progress * 0.5f);

		lid.setTransform(stack);

		stack.popPose();
	}

	@Override
	public void remove() {
		base.delete();
		lid.delete();
	}

	@Override
	public void updateLight() {
		relight(pos, base, lid);
	}

	private ModelData makeBaseInstance() {
        return materialManager.cutout(RenderType.entityCutoutNoCull(Sheets.SHULKER_SHEET))
                .material(Materials.TRANSFORMED)
				.model("base_" + texture.contents().name(), this::makeBaseModel)
				.createInstance();
	}

	private ModelData makeLidInstance() {
        return materialManager.cutout(RenderType.entityCutoutNoCull(Sheets.SHULKER_SHEET))
                .material(Materials.TRANSFORMED)
				.model("lid_" + texture.contents().name(), this::makeLidModel)
				.createInstance();
	}

	private ModelPart makeBaseModel() {
		return ModelPart.builder("shulker_base", 64, 64)
				.sprite(texture)
				.cuboid()
				.textureOffset(0, 28)
				.size(16, 8, 16)
				.invertYZ()
				.endCuboid()
				.build();
	}

	private ModelPart makeLidModel() {
		return ModelPart.builder("shulker_lid", 64, 64)
				.sprite(texture)
				.cuboid()
				.size(16, 12, 16)
				.invertYZ()
				.endCuboid()
				.build();
	}

	private Direction getDirection() {
		if (blockState.getBlock() instanceof ShulkerBoxBlock) {
			return blockState.getValue(ShulkerBoxBlock.FACING);
		}

		return Direction.UP;
	}
}
