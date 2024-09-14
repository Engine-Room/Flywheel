package dev.engine_room.flywheel.vanilla;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.RetexturedMesh;
import dev.engine_room.flywheel.lib.model.part.InstanceTree;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ChestVisual<T extends BlockEntity & LidBlockEntity> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual {
	private static final dev.engine_room.flywheel.api.material.Material MATERIAL = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.texture(Sheets.CHEST_SHEET)
			.mipmap(false)
			.build();

	private static final Map<ChestType, ModelLayerLocation> LAYER_LOCATIONS = new EnumMap<>(ChestType.class);
	static {
		LAYER_LOCATIONS.put(ChestType.SINGLE, ModelLayers.CHEST);
		LAYER_LOCATIONS.put(ChestType.LEFT, ModelLayers.DOUBLE_CHEST_LEFT);
		LAYER_LOCATIONS.put(ChestType.RIGHT, ModelLayers.DOUBLE_CHEST_RIGHT);
	}

	@Nullable
	private final InstanceTree instances;
	@Nullable
	private final InstanceTree lid;
	@Nullable
	private final InstanceTree lock;

	private final PoseStack poseStack = new PoseStack();
	private final BrightnessCombiner brightnessCombiner = new BrightnessCombiner();
	@Nullable
	private final DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborCombineResult;
	@Nullable
	private final Float2FloatFunction lidProgress;

	private float lastProgress = Float.NaN;

	public ChestVisual(VisualizationContext ctx, T blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		Block block = blockState.getBlock();
		if (block instanceof AbstractChestBlock<?> chestBlock) {
			ChestType chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
			TextureAtlasSprite sprite = Sheets.chooseMaterial(blockEntity, chestType, isChristmas()).sprite();

			instances = InstanceTree.create(instancerProvider, LAYER_LOCATIONS.get(chestType), (path, mesh) -> {
				return new Model.ConfiguredMesh(MATERIAL, new RetexturedMesh(mesh, sprite));
			});
			lid = instances.childOrThrow("lid");
			lock = instances.childOrThrow("lock");

			poseStack.pushPose();
			TransformStack.of(poseStack).translate(getVisualPosition());
			float horizontalAngle = blockState.getValue(ChestBlock.FACING).toYRot();
			poseStack.translate(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(Axis.YP.rotationDegrees(-horizontalAngle));
			poseStack.translate(-0.5F, -0.5F, -0.5F);

			neighborCombineResult = chestBlock.combine(blockState, level, pos, true);
			lidProgress = neighborCombineResult.apply(ChestBlock.opennessCombiner(blockEntity));

			lastProgress = lidProgress.get(partialTick);
			applyLidTransform(lastProgress);
		} else {
			instances = null;
			lid = null;
			lock = null;
			neighborCombineResult = null;
			lidProgress = null;
		}
	}

	private static boolean isChristmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
	}

	@Override
	public void setSectionCollector(SectionCollector sectionCollector) {
		this.lightSections = sectionCollector;

		if (neighborCombineResult != null) {
			lightSections.sections(neighborCombineResult.apply(new SectionPosCombiner()));
		} else {
			lightSections.sections(LongSet.of(SectionPos.asLong(pos)));
		}
	}

	@Override
	public void beginFrame(Context context) {
		if (instances == null) {
			return;
		}

		if (doDistanceLimitThisFrame(context) || !isVisible(context.frustum())) {
			return;
		}

		float progress = lidProgress.get(context.partialTick());
		if (lastProgress == progress) {
			return;
		}
		lastProgress = progress;

		applyLidTransform(progress);
	}

	private void applyLidTransform(float progress) {
		progress = 1.0F - progress;
		progress = 1.0F - progress * progress * progress;

		lid.xRot = -(progress * ((float) Math.PI / 2F));
		lock.xRot = lid.xRot;
		instances.updateInstances(poseStack);
	}

	@Override
	public void updateLight(float partialTick) {
		if (instances != null) {
			int packedLight = neighborCombineResult.apply(brightnessCombiner);
			instances.traverse(instance -> {
				instance.light(packedLight)
						.setChanged();
			});
		}
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		if (instances != null) {
			instances.traverse(consumer);
		}
	}

	@Override
	protected void _delete() {
		if (instances != null) {
			instances.delete();
		}
	}

	private class SectionPosCombiner implements DoubleBlockCombiner.Combiner<BlockEntity, LongSet> {
		@Override
		public LongSet acceptDouble(BlockEntity first, BlockEntity second) {
			long firstSection = SectionPos.asLong(first.getBlockPos());
			long secondSection = SectionPos.asLong(second.getBlockPos());

			if (firstSection == secondSection) {
				return LongSet.of(firstSection);
			} else {
				return LongSet.of(firstSection, secondSection);
			}
		}

		@Override
		public LongSet acceptSingle(BlockEntity single) {
			return LongSet.of(SectionPos.asLong(single.getBlockPos()));
		}

		@Override
		public LongSet acceptNone() {
			return LongSet.of(SectionPos.asLong(pos));
		}
	}

	private class BrightnessCombiner implements DoubleBlockCombiner.Combiner<BlockEntity, Integer> {
		@Override
		public Integer acceptDouble(BlockEntity first, BlockEntity second) {
			int firstLight = LevelRenderer.getLightColor(first.getLevel(), first.getBlockPos());
			int secondLight = LevelRenderer.getLightColor(second.getLevel(), second.getBlockPos());
			int firstBlockLight = LightTexture.block(firstLight);
			int secondBlockLight = LightTexture.block(secondLight);
			int firstSkyLight = LightTexture.sky(firstLight);
			int secondSkyLight = LightTexture.sky(secondLight);
			return LightTexture.pack(Math.max(firstBlockLight, secondBlockLight), Math.max(firstSkyLight, secondSkyLight));
		}

		@Override
		public Integer acceptSingle(BlockEntity single) {
			return LevelRenderer.getLightColor(single.getLevel(), single.getBlockPos());
		}

		@Override
		public Integer acceptNone() {
			return LevelRenderer.getLightColor(level, pos);
		}
	}
}
