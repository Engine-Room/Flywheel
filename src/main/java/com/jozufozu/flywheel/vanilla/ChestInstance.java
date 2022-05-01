package com.jozufozu.flywheel.vanilla;

import java.util.Calendar;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.BasicModelSupplier;
import com.jozufozu.flywheel.core.hardcoded.ModelPart;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.Util;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ChestInstance<T extends BlockEntity & LidBlockEntity> extends BlockEntityInstance<T> implements DynamicInstance {

	private static final BiFunction<ChestType, Material, BasicModelSupplier> LID = Util.memoize((type, mat) -> new BasicModelSupplier(() -> createLidModel(type, mat.sprite()), Sheets.chestSheet()));
	private static final BiFunction<ChestType, Material, BasicModelSupplier> BASE = Util.memoize((type, mat) -> new BasicModelSupplier(() -> createBaseModel(type, mat.sprite()), Sheets.chestSheet()));

	private final OrientedData body;
	private final ModelData lid;

	private final Float2FloatFunction lidProgress;
	private final Material renderMaterial;
	@Nonnull
	private final ChestType chestType;
	private final Quaternion baseRotation;

	private float lastProgress = Float.NaN;

	public ChestInstance(MaterialManager materialManager, T blockEntity) {
		super(materialManager, blockEntity);

		Block block = blockState.getBlock();

		chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		renderMaterial = Sheets.chooseMaterial(blockEntity, chestType, isChristmas());

		body = baseInstance()
				.setPosition(getInstancePosition());
		lid = lidInstance();

		if (block instanceof AbstractChestBlock<?> chestBlock) {

			float horizontalAngle = blockState.getValue(ChestBlock.FACING).toYRot();

			baseRotation = Vector3f.YP.rotationDegrees(-horizontalAngle);

			body.setRotation(baseRotation);

			DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> wrapper = chestBlock.combine(blockState, world, getWorldPosition(), true);

			this.lidProgress = wrapper.apply(ChestBlock.opennessCombiner(blockEntity));


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

		lid.loadIdentity()
				.translate(getInstancePosition())
				.translate(0, 9f/16f, 0)
				.centre()
				.multiply(baseRotation)
				.unCentre()
				.translate(0, 0, 1f / 16f)
				.multiply(Vector3f.XP.rotation(angleX))
				.translate(0, 0, -1f / 16f);
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

		return materialManager.material(Materials.ORIENTED)
				.model(BASE.apply(chestType, renderMaterial))
				.createInstance();
	}

	private ModelData lidInstance() {

		return materialManager.material(Materials.TRANSFORMED)
				.model(LID.apply(chestType, renderMaterial))
				.createInstance();
	}

	private static ModelPart createBaseModel(ChestType type, TextureAtlasSprite sprite) {

		return switch (type) {
			case LEFT -> ModelPart.builder("chest_base_left", 64, 64)
					.sprite(sprite)
					.cuboid()
					.textureOffset(0, 19)
					.start(0, 0, 1)
					.size(15, 10, 14)
					.endCuboid()
					.build();
			case RIGHT -> ModelPart.builder("chest_base_right", 64, 64)
					.sprite(sprite)
					.cuboid()
					.textureOffset(0, 19)
					.start(1, 0, 1)
					.size(15, 10, 14)
					.endCuboid()
					.build();
			default -> ModelPart.builder("chest_base", 64, 64)
					.sprite(sprite)
					.cuboid()
					.textureOffset(0, 19)
					.start(1, 0, 1)
					.end(15, 10, 15)
					.endCuboid()
					.build();
		};

	}

	private static ModelPart createLidModel(ChestType type, TextureAtlasSprite sprite) {

		return switch (type) {
			case LEFT -> ModelPart.builder("chest_lid_left", 64, 64)
					.sprite(sprite)
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
			case RIGHT -> ModelPart.builder("chest_lid_right", 64, 64)
					.sprite(sprite)
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
			default -> ModelPart.builder("chest_lid", 64, 64)
					.sprite(sprite)
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
		};

	}

	public static boolean isChristmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
	}
}
