package com.jozufozu.flywheel.vanilla;

import java.util.Calendar;
import java.util.List;
import java.util.function.BiFunction;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.OrientedInstance;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.model.SimpleLazyModel;
import com.jozufozu.flywheel.lib.modelpart.ModelPart;
import com.jozufozu.flywheel.lib.util.AnimationTickHolder;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.Util;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ChestVisual<T extends BlockEntity & LidBlockEntity> extends AbstractBlockEntityVisual<T> implements DynamicVisual {
	private static final BiFunction<ChestType, TextureAtlasSprite, SimpleLazyModel> BODY_MODEL_FUNC = Util.memoize((type, mat) -> new SimpleLazyModel(() -> createBodyMesh(type, mat), Materials.CHEST));
	private static final BiFunction<ChestType, TextureAtlasSprite, SimpleLazyModel> LID_MODEL_FUNC = Util.memoize((type, mat) -> new SimpleLazyModel(() -> createLidMesh(type, mat), Materials.CHEST));

	private OrientedInstance body;
	private TransformedInstance lid;

	private Float2FloatFunction lidProgress;
	private TextureAtlasSprite sprite;
	private ChestType chestType;
	private Quaternion baseRotation;

	private float lastProgress = Float.NaN;

	public ChestVisual(VisualizationContext ctx, T blockEntity) {
		super(ctx, blockEntity);
	}

	@Override
	public void init() {
		Block block = blockState.getBlock();

		chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		sprite = Sheets.chooseMaterial(blockEntity, chestType, isChristmas())
				.sprite();

		body = createBodyInstance().setPosition(getVisualPosition());
		lid = createLidInstance();

		if (block instanceof AbstractChestBlock<?> chestBlock) {
			float horizontalAngle = blockState.getValue(ChestBlock.FACING).toYRot();

			baseRotation = Vector3f.YP.rotationDegrees(-horizontalAngle);

			body.setRotation(baseRotation);

			DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> wrapper = chestBlock.combine(blockState, level, pos, true);

			this.lidProgress = wrapper.apply(ChestBlock.opennessCombiner(blockEntity));
		} else {
			baseRotation = Quaternion.ONE;
			lidProgress = $ -> 0f;
		}

		super.init();
	}

	@Override
	public void beginFrame() {
		float progress = lidProgress.get(AnimationTickHolder.getPartialTicks());

		if (lastProgress == progress) {
			return;
		}

		lastProgress = progress;

		progress = 1.0F - progress;
		progress = 1.0F - progress * progress * progress;

		float angleX = -(progress * ((float) Math.PI / 2F));

		lid.loadIdentity()
				.translate(getVisualPosition())
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
		relight(pos, body, lid);
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

	private OrientedInstance createBodyInstance() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, BODY_MODEL_FUNC.apply(chestType, sprite), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private TransformedInstance createLidInstance() {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, LID_MODEL_FUNC.apply(chestType, sprite), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private static ModelPart createBodyMesh(ChestType type, TextureAtlasSprite sprite) {
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

	private static ModelPart createLidMesh(ChestType type, TextureAtlasSprite sprite) {
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
