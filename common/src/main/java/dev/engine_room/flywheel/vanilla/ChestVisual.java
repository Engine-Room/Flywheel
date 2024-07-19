package dev.engine_room.flywheel.vanilla;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import org.joml.Quaternionf;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.model.ModelCache;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.model.part.ModelPartConverter;
import dev.engine_room.flywheel.lib.util.Pair;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
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

public class ChestVisual<T extends BlockEntity & LidBlockEntity> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual {
	private static final Map<ChestType, ModelLayerLocation> LAYER_LOCATIONS = new EnumMap<>(ChestType.class);
	static {
		LAYER_LOCATIONS.put(ChestType.SINGLE, ModelLayers.CHEST);
		LAYER_LOCATIONS.put(ChestType.LEFT, ModelLayers.DOUBLE_CHEST_LEFT);
		LAYER_LOCATIONS.put(ChestType.RIGHT, ModelLayers.DOUBLE_CHEST_RIGHT);
	}

	private static final ModelCache<Pair<ChestType, Material>> BOTTOM_MODELS = new ModelCache<>(key -> {
		return new SingleMeshModel(ModelPartConverter.convert(LAYER_LOCATIONS.get(key.first()), key.second().sprite(), "bottom"), Materials.CHEST);
	});
	private static final ModelCache<Pair<ChestType, Material>> LID_MODELS = new ModelCache<>(key -> {
		return new SingleMeshModel(ModelPartConverter.convert(LAYER_LOCATIONS.get(key.first()), key.second().sprite(), "lid"), Materials.CHEST);
	});
	private static final ModelCache<Pair<ChestType, Material>> LOCK_MODELS = new ModelCache<>(key -> {
		return new SingleMeshModel(ModelPartConverter.convert(LAYER_LOCATIONS.get(key.first()), key.second().sprite(), "lock"), Materials.CHEST);
	});

	private final OrientedInstance bottom;
	private final TransformedInstance lid;
	private final TransformedInstance lock;

	private final ChestType chestType;
	private final Float2FloatFunction lidProgress;

	private final Quaternionf baseRotation = new Quaternionf();

	private float lastProgress = Float.NaN;

	public ChestVisual(VisualizationContext ctx, T blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		Material texture = Sheets.chooseMaterial(blockEntity, chestType, isChristmas());

		bottom = createBottomInstance(texture).setPosition(getVisualPosition());
		lid = createLidInstance(texture);
		lock = createLockInstance(texture);

		Block block = blockState.getBlock();
		if (block instanceof AbstractChestBlock<?> chestBlock) {
			float horizontalAngle = blockState.getValue(ChestBlock.FACING).toYRot();
			baseRotation.setAngleAxis(Math.toRadians(-horizontalAngle), 0, 1, 0);

			DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> wrapper = chestBlock.combine(blockState, level, pos, true);
			lidProgress = wrapper.apply(ChestBlock.opennessCombiner(blockEntity));
		} else {
			baseRotation.identity();
			lidProgress = $ -> 0f;
		}

		bottom.setRotation(baseRotation);
		bottom.setChanged();

		applyLidTransform(lidProgress.get(partialTick));
	}

	private OrientedInstance createBottomInstance(Material texture) {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, BOTTOM_MODELS.get(Pair.of(chestType, texture)))
				.createInstance();
	}

	private TransformedInstance createLidInstance(Material texture) {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, LID_MODELS.get(Pair.of(chestType, texture)))
				.createInstance();
	}

	private TransformedInstance createLockInstance(Material texture) {
		return instancerProvider.instancer(InstanceTypes.TRANSFORMED, LOCK_MODELS.get(Pair.of(chestType, texture)))
				.createInstance();
	}

	private static boolean isChristmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
	}

	@Override
	public void beginFrame(Context context) {
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

		float angleX = -(progress * ((float) Math.PI / 2F));

		lid.loadIdentity()
				.translate(getVisualPosition())
				.rotateCentered(baseRotation)
				.translate(0, 9f / 16f, 1f / 16f)
				.rotateX(angleX)
				.translate(0, -9f / 16f, -1f / 16f)
				.setChanged();

		lock.loadIdentity()
				.translate(getVisualPosition())
				.rotateCentered(baseRotation)
				.translate(0, 8f / 16f, 0)
				.rotateX(angleX)
				.translate(0, -8f / 16f, 0)
				.setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(bottom, lid, lock);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(bottom);
		consumer.accept(lid);
		consumer.accept(lock);
	}

	@Override
	protected void _delete() {
		bottom.delete();
		lid.delete();
		lock.delete();
	}
}
