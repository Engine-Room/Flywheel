package com.jozufozu.flywheel.vanilla;

import java.util.Calendar;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.state.TextureRenderState;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.jozufozu.flywheel.core.model.ModelPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;

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
import net.minecraft.util.math.vector.Vector3f;

public class ChestInstance<T extends TileEntity & IChestLid> extends TileEntityInstance<T> implements IDynamicInstance {

	private final MatrixTransformStack stack = new MatrixTransformStack();
	private final OrientedData body;
	private final ModelData lid;

	private final Float2FloatFunction lidProgress;
	private final RenderMaterial renderMaterial;
	@Nonnull
	private final ChestType chestType;
	private final Quaternion baseRotation;

	private float lastProgress = Float.NaN;

	public ChestInstance(MaterialManager materialManager, T tile) {
		super(materialManager, tile);

		Block block = blockState.getBlock();

		chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		renderMaterial = Atlases.chooseMaterial(tile, chestType, isChristmas());

		body = baseInstance()
				.setPosition(getInstancePosition());
		lid = lidInstance();

		if (block instanceof AbstractChestBlock) {

//			MatrixStack stack = new MatrixStack();
//
//			stack.push();
			float horizontalAngle = blockState.getValue(ChestBlock.FACING).toYRot();

			baseRotation = Vector3f.YP.rotationDegrees(-horizontalAngle);

			body.setRotation(baseRotation);

			AbstractChestBlock<?> chestBlock = (AbstractChestBlock<?>) block;

			TileEntityMerger.ICallbackWrapper<? extends ChestTileEntity> wrapper = chestBlock.combine(blockState, world, getWorldPosition(), true);

			this.lidProgress = wrapper.apply(ChestBlock.opennessCombiner(tile));


		} else {
			baseRotation = Quaternion.ONE;
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

		stack.setIdentity()
				.translate(getInstancePosition())
				.translate(0, 9f/16f, 0)
				.centre()
				.multiply(baseRotation)
				.unCentre()
				.translate(0, 0, 1f / 16f)
				.multiply(Vector3f.XP.rotation(angleX))
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

		return materialManager.solid(TextureRenderState.get(renderMaterial.atlasLocation()))
                .material(Materials.ORIENTED)
				.model("base_" + renderMaterial.texture(), this::getBaseModel)
				.createInstance();
	}

	private ModelData lidInstance() {

		return materialManager.solid(TextureRenderState.get(renderMaterial.atlasLocation()))
                .material(Materials.TRANSFORMED)
				.model("lid_" + renderMaterial.texture(), this::getLidModel)
				.createInstance();
	}

	private ModelPart getBaseModel() {

		switch (chestType) {
		case LEFT:
			return ModelPart.builder(64, 64)
				.sprite(renderMaterial.sprite())
				.cuboid()
				.textureOffset(0, 19)
				.start(0, 0, 1)
				.size(15, 10, 14)
				.endCuboid()
				.build();
		case RIGHT:
			return ModelPart.builder(64, 64)
				.sprite(renderMaterial.sprite())
				.cuboid()
				.textureOffset(0, 19)
				.start(1, 0, 1)
				.size(15, 10, 14)
				.endCuboid()
				.build();
		}

		return ModelPart.builder(64, 64)
				.sprite(renderMaterial.sprite())
				.cuboid()
				.textureOffset(0, 19)
				.start(1, 0, 1)
				.end(15, 10, 15)
				.endCuboid()
				.build();
	}

	private ModelPart getLidModel() {

		switch (chestType) {
		case LEFT:
		return ModelPart.builder(64, 64)
				.sprite(renderMaterial.sprite())
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
				.sprite(renderMaterial.sprite())
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
				.sprite(renderMaterial.sprite())
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
