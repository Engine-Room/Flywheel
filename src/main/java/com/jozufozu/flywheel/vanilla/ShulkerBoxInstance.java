package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.api.instance.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.jozufozu.flywheel.backend.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.model.ModelPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class ShulkerBoxInstance extends TileEntityInstance<ShulkerBoxBlockEntity> implements IDynamicInstance {

	private final TextureAtlasSprite texture;

	private final ModelData base;
	private final ModelData lid;
	private final MatrixTransformStack stack = new MatrixTransformStack();

	private float lastProgress = Float.NaN;

	public ShulkerBoxInstance(MaterialManager materialManager, ShulkerBoxBlockEntity tile) {
		super(materialManager, tile);

		DyeColor color = tile.getColor();
		if (color == null) {
			texture = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.sprite();
		} else {
			texture = Sheets.SHULKER_TEXTURE_LOCATION.get(color.getId()).sprite();
		}
		Quaternion rotation = getDirection().getRotation();

		stack.translate(getInstancePosition())
				.scale(0.9995f)
				.translateAll(0.00025)
				.centre()
				.multiply(rotation)
				.unCentre();

		base = makeBaseInstance().setTransform(stack.unwrap());

		stack.translateY(0.25);

		lid = makeLidInstance().setTransform(stack.unwrap());
	}

	@Override
	public void beginFrame() {
		float progress = tile.getProgress(AnimationTickHolder.getPartialTicks());

		if (progress == lastProgress) return;
		lastProgress = progress;

		Quaternion spin = Vector3f.YP.rotationDegrees(270.0F * progress);

		stack.pushPose()
				.centre()
				.multiply(spin)
				.unCentre()
				.translateY(progress * 0.5f);

		lid.setTransform(stack.unwrap());

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
				.model("base_" + texture.getName(), this::makeBaseModel)
				.createInstance();
	}

	private ModelData makeLidInstance() {
        return materialManager.cutout(RenderType.entityCutoutNoCull(Sheets.SHULKER_SHEET))
                .material(Materials.TRANSFORMED)
				.model("lid_" + texture.getName(), this::makeLidModel)
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
