package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;

import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.materials.OrientedData;

import com.jozufozu.flywheel.core.model.ModelPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.jozufozu.flywheel.util.vec.Vec3;
import com.jozufozu.flywheel.util.vec.Vec4;
import com.mojang.blaze3d.matrix.MatrixStack;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMerger;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

import javax.annotation.Nonnull;

import java.util.Calendar;

public class ChestInstance<T extends TileEntity & IChestLid> extends TileEntityInstance<T> implements IDynamicInstance {

	private final OrientedData body;
	private final ModelData lid;

	private final Float2FloatFunction lidProgress;
	private final RenderMaterial renderMaterial;
	@Nonnull
	private final ChestType chestType;
	private final Quaternion baseRotation;

	private float lastProgress = Float.NaN;

	public ChestInstance(MaterialManager<?> materialManager, T tile) {
		super(materialManager, tile);

		Block block = blockState.getBlock();

		chestType = blockState.contains(ChestBlock.TYPE) ? blockState.get(ChestBlock.TYPE) : ChestType.SINGLE;
		renderMaterial = Atlases.getChestTexture(tile, chestType, isChristmas());

		body = baseInstance()
				.setPosition(getInstancePosition());
		lid = lidInstance();

		if (block instanceof AbstractChestBlock) {

//			MatrixStack stack = new MatrixStack();
//
//			stack.push();
			float horizontalAngle = blockState.get(ChestBlock.FACING).getHorizontalAngle();

			baseRotation = Vector3f.POSITIVE_Y.getDegreesQuaternion(-horizontalAngle);

			body.setRotation(baseRotation);

			AbstractChestBlock<?> chestBlock = (AbstractChestBlock<?>) block;

			TileEntityMerger.ICallbackWrapper<? extends ChestTileEntity> wrapper = chestBlock.getBlockEntitySource(blockState, world, getWorldPosition(), true);

			this.lidProgress = wrapper.apply(ChestBlock.getAnimationProgressRetriever(tile));


		} else {
			baseRotation = Quaternion.IDENTITY;
			lidProgress = $ -> 0f;
		}
	}

	@Override
	public void beginFrame() {
		float progress = lidProgress.get(AnimationTickHolder.getPartialTicks());

		if (lastProgress == progress) return;

		lastProgress = progress;

		progress = 1.0F - progress;
		progress = 1.0F - progress * progress * progress;

		float angleX = -(progress * ((float) Math.PI / 2F));

		MatrixTransformStack stack = new MatrixTransformStack();

		stack.translate(getInstancePosition())
				.translate(0, 9f/16f, 0)
				.centre()
				.multiply(baseRotation)
				.unCentre()
				.translate(0, 0, 1f / 16f)
				.multiply(Vector3f.POSITIVE_X.getRadialQuaternion(angleX))
				.translate(0, 0, -1f / 16f);

		lid.setTransform(stack.unwrap());

	}

	@Override
	public void updateLight() {
		relight(getWorldPosition(), body, lid);
	}

	@Override
	public void remove() {
		body.delete();
		lid.delete();
	}

	private OrientedData baseInstance() {

		return materialManager.getMaterial(Materials.ORIENTED, renderMaterial.getAtlasId())
				.get("base_" + renderMaterial.getTextureId(), this::getBaseModel)
				.createInstance();
	}

	private ModelData lidInstance() {

		return materialManager.getMaterial(Materials.TRANSFORMED, renderMaterial.getAtlasId())
				.get("lid_" + renderMaterial.getTextureId(), this::getLidModel)
				.createInstance();
	}

	private BufferedModel getBaseModel() {

		switch (chestType) {
		case LEFT:
			return ModelPart.builder(64, 64)
				.sprite(renderMaterial.getSprite())
				.cuboid()
				.textureOffset(0, 19)
				.start(0, 0, 1)
				.size(15, 10, 14)
				.endCuboid()
				.build();
		case RIGHT:
			return ModelPart.builder(64, 64)
				.sprite(renderMaterial.getSprite())
				.cuboid()
				.textureOffset(0, 19)
				.start(1, 0, 1)
				.size(15, 10, 14)
				.endCuboid()
				.build();
		}

		return ModelPart.builder(64, 64)
				.sprite(renderMaterial.getSprite())
				.cuboid()
				.textureOffset(0, 19)
				.start(1, 0, 1)
				.end(15, 10, 15)
				.endCuboid()
				.build();
	}

	private BufferedModel getLidModel() {

		switch (chestType) {
		case LEFT:
		return ModelPart.builder(64, 64)
				.sprite(renderMaterial.getSprite())
				.cuboid()
				.textureOffset(0, 0)
				.start(0, 0, 1)
				.size(15, 5, 14)
				.endCuboid()
				.cuboid()
				.start(0, -2, 15)
				.size(1, 4, 1)
				.endCuboid()
				.build();
		case RIGHT:
		return ModelPart.builder(64, 64)
				.sprite(renderMaterial.getSprite())
				.cuboid()
				.textureOffset(0, 0)
				.start(1, 0, 1)
				.size(15, 5, 14)
				.endCuboid()
				.cuboid()
				.start(15, -2, 15)
				.size(1, 4, 1)
				.endCuboid()
				.build();
		}

		return ModelPart.builder(64, 64)
				.sprite(renderMaterial.getSprite())
				.cuboid()
				.textureOffset(0, 0)
				.start(1, 0, 1)
				.size(14, 5, 14)
				.endCuboid()
				.cuboid()
				.start(7, -2, 15)
				.size(2, 4, 1)
				.endCuboid()
				.build();
	}

	public static boolean isChristmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
	}
}
