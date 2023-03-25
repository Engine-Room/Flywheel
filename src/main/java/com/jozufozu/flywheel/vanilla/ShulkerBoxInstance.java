package com.jozufozu.flywheel.vanilla;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.InstancerManager;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.hardcoded.ModelPart;
import com.jozufozu.flywheel.core.model.SimpleLazyModel;
import com.jozufozu.flywheel.core.structs.StructTypes;
import com.jozufozu.flywheel.core.structs.transformed.TransformedPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.TransformStack;
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

public class ShulkerBoxInstance extends BlockEntityInstance<ShulkerBoxBlockEntity> implements DynamicInstance {

	private static final Function<TextureAtlasSprite, SimpleLazyModel> BASE = Util.memoize(it -> new SimpleLazyModel(() -> makeBaseModel(it), Materials.SHULKER));
	private static final Function<TextureAtlasSprite, SimpleLazyModel> LID = Util.memoize(it -> new SimpleLazyModel(() -> makeLidModel(it), Materials.SHULKER));

	private final TextureAtlasSprite texture;

	private final TransformedPart base;
	private final TransformedPart lid;
	private final PoseStack stack = new PoseStack();

	private float lastProgress = Float.NaN;

	public ShulkerBoxInstance(InstancerManager instancerManager, ShulkerBoxBlockEntity blockEntity) {
		super(instancerManager, blockEntity);

		DyeColor color = blockEntity.getColor();
		if (color == null) {
			texture = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.sprite();
		} else {
			texture = Sheets.SHULKER_TEXTURE_LOCATION.get(color.getId()).sprite();
		}
		Quaternion rotation = getDirection().getRotation();

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
	public void addCrumblingParts(List<InstancedPart> data) {
		Collections.addAll(data, base, lid);
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

	private TransformedPart makeBaseInstance() {
		return instancerManager.instancer(StructTypes.TRANSFORMED, BASE.apply(texture), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private TransformedPart makeLidInstance() {
		return instancerManager.instancer(StructTypes.TRANSFORMED, LID.apply(texture), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private static ModelPart makeBaseModel(TextureAtlasSprite texture) {
		return ModelPart.builder("shulker_base", 64, 64)
				.sprite(texture)
				.cuboid()
				.textureOffset(0, 28)
				.size(16, 8, 16)
				.invertYZ()
				.endCuboid()
				.build();
	}

	private static ModelPart makeLidModel(TextureAtlasSprite texture) {
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
