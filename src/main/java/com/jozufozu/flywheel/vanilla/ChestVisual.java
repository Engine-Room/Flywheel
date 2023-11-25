package com.jozufozu.flywheel.vanilla;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.OrientedInstance;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.jozufozu.flywheel.lib.model.part.ModelPartConverter;
import com.jozufozu.flywheel.lib.util.Pair;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ChestVisual<T extends BlockEntity & LidBlockEntity> extends AbstractBlockEntityVisual<T> implements DynamicVisual {
	private static final Map<ChestType, ModelLayerLocation> LAYER_LOCATIONS = new EnumMap<>(ChestType.class);
	static {
		LAYER_LOCATIONS.put(ChestType.SINGLE, ModelLayers.CHEST);
		LAYER_LOCATIONS.put(ChestType.LEFT, ModelLayers.DOUBLE_CHEST_LEFT);
		LAYER_LOCATIONS.put(ChestType.RIGHT, ModelLayers.DOUBLE_CHEST_RIGHT);
	}

	private static final ModelCache<Pair<ChestType, Material>> BOTTOM_MODELS = new ModelCache<>(key -> {
		return new SimpleModel(ModelPartConverter.convert(LAYER_LOCATIONS.get(key.first()), key.second().sprite(), "bottom"), Materials.CHEST);
	});
	private static final ModelCache<Pair<ChestType, Material>> LID_MODELS = new ModelCache<>(key -> {
		return new SimpleModel(ModelPartConverter.convert(LAYER_LOCATIONS.get(key.first()), key.second().sprite(), "lid"), Materials.CHEST);
	});
	private static final ModelCache<Pair<ChestType, Material>> LOCK_MODELS = new ModelCache<>(key -> {
		return new SimpleModel(ModelPartConverter.convert(LAYER_LOCATIONS.get(key.first()), key.second().sprite(), "lock"), Materials.CHEST);
	});

	private OrientedInstance bottom;
	private TransformedInstance lid;
	private TransformedInstance lock;

	private ChestType chestType;
	private Material texture;
	private final Quaternionf baseRotation = new Quaternionf();
	private Float2FloatFunction lidProgress;

	private float lastProgress = Float.NaN;

	public ChestVisual(VisualizationContext ctx, T blockEntity) {
		super(ctx, blockEntity);
	}

	@Override
	public void init(float partialTick) {
		chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		texture = Sheets.chooseMaterial(blockEntity, chestType, isChristmas());

		bottom = createBottomInstance().setPosition(getVisualPosition());
		lid = createLidInstance();
		lock = createLockInstance();

		Block block = blockState.getBlock();
		if (block instanceof AbstractChestBlock<?> chestBlock) {
			float horizontalAngle = blockState.getValue(ChestBlock.FACING).toYRot();
			baseRotation.setAngleAxis(Math.toRadians(-horizontalAngle), 0, 1, 0);
			bottom.setRotation(baseRotation);

			DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> wrapper = chestBlock.combine(blockState, level, pos, true);
			lidProgress = wrapper.apply(ChestBlock.opennessCombiner(blockEntity));
		} else {
			baseRotation.identity();
			lidProgress = $ -> 0f;
		}

		super.init(partialTick);
	}

	private OrientedInstance createBottomInstance() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, BOTTOM_MODELS.get(Pair.of(chestType, texture)), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private TransformedInstance createLidInstance() {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, LID_MODELS.get(Pair.of(chestType, texture)), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private TransformedInstance createLockInstance() {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, LOCK_MODELS.get(Pair.of(chestType, texture)), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	private static boolean isChristmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
	}

	@Override
	public void beginFrame(VisualFrameContext context) {
		if (doDistanceLimitThisFrame(context) || !isVisible(context.frustum())) {
			return;
		}

		float progress = lidProgress.get(context.partialTick());
		if (lastProgress == progress) {
			return;
		}
		lastProgress = progress;

		progress = 1.0F - progress;
		progress = 1.0F - progress * progress * progress;

		float angleX = -(progress * ((float) Math.PI / 2F));

		lid.loadIdentity()
				.translate(getVisualPosition())
				.rotateCentered(baseRotation)
				.translate(0, 9f / 16f, 1f / 16f)
				.rotateX(angleX)
				.translate(0, -9f / 16f, -1f / 16f);

		lock.loadIdentity()
				.translate(getVisualPosition())
				.rotateCentered(baseRotation)
				.translate(0, 8f / 16f, 0)
				.rotateX(angleX)
				.translate(0, -8f / 16f, 0);
	}

	@Override
	public void updateLight() {
		relight(pos, bottom, lid, lock);
	}

	@Override
	public List<Instance> getCrumblingInstances() {
		return List.of(bottom, lid, lock);
	}

	@Override
	protected void _delete() {
		bottom.delete();
		lid.delete();
		lock.delete();
	}
}
