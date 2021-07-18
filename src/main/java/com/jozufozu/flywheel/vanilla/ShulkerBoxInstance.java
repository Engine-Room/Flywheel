package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.model.ModelPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class ShulkerBoxInstance extends TileEntityInstance<ShulkerBoxTileEntity> implements IDynamicInstance {

	private final TextureAtlasSprite texture;

	private final ModelData base;
	private final ModelData lid;
	private final MatrixTransformStack stack;

	private float lastProgress = Float.NaN;

	public ShulkerBoxInstance(MaterialManager<?> materialManager, ShulkerBoxTileEntity tile) {
		super(materialManager, tile);

		DyeColor color = tile.getColor();
		if (color == null) {
			texture = Atlases.DEFAULT_SHULKER_TEXTURE_LOCATION.sprite();
		} else {
			texture = Atlases.SHULKER_TEXTURE_LOCATION.get(color.getId()).sprite();
		}
		Quaternion rotation = getDirection().getRotation();

		stack = new MatrixTransformStack();

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

		stack.push()
				.centre()
				.multiply(spin)
				.unCentre()
				.translateY(progress * 0.5f);

		lid.setTransform(stack.unwrap());

		stack.pop();
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
		return materialManager.getMaterial(Materials.TRANSFORMED, RenderStates.SHULKER)
				.get("base_" + texture.getName(), this::makeBaseModel)
				.createInstance();
	}

	private ModelData makeLidInstance() {
		return materialManager.getMaterial(Materials.TRANSFORMED, RenderStates.SHULKER)
				.get("lid_" + texture.getName(), this::makeLidModel)
				.createInstance();
	}

	private BufferedModel makeBaseModel() {
		return ModelPart.builder(64, 64)
				.sprite(texture)
				.cuboid()
				.textureOffset(0, 28)
				.size(16, 8, 16)
				.invertYZ()
				.endCuboid()
				.build();
	}

	private BufferedModel makeLidModel() {
		return ModelPart.builder(64, 64)
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
