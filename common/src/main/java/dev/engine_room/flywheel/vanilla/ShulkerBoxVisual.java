package dev.engine_room.flywheel.vanilla;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.joml.Matrix4f;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.ResourceReloadCache;
import dev.engine_room.flywheel.lib.model.part.InstanceTree;
import dev.engine_room.flywheel.lib.model.part.LoweringVisitor;
import dev.engine_room.flywheel.lib.model.part.ModelTree;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class ShulkerBoxVisual extends AbstractBlockEntityVisual<ShulkerBoxBlockEntity> implements SimpleDynamicVisual {
	private static final dev.engine_room.flywheel.api.material.Material MATERIAL = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.texture(Sheets.SHULKER_SHEET)
			.mipmap(false)
			.backfaceCulling(false)
			.build();


	private static final Function<Material, LoweringVisitor> VISITORS = new ResourceReloadCache<>(m -> LoweringVisitor.pruning(Set.of("head"), MATERIAL, m.sprite()));

	private final InstanceTree instances;
	private final InstanceTree lid;

	private final Matrix4f initialPose;

	private float lastProgress = Float.NaN;

	public ShulkerBoxVisual(VisualizationContext ctx, ShulkerBoxBlockEntity blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		DyeColor color = blockEntity.getColor();
		Material texture;
		if (color == null) {
			texture = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION;
		} else {
			texture = Sheets.SHULKER_TEXTURE_LOCATION.get(color.getId());
		}

		instances = InstanceTree.create(instancerProvider(), ModelTree.of(ModelLayers.SHULKER, VISITORS.apply(texture)));

		initialPose = createInitialPose();

		lid = instances.childOrThrow("lid");
	}

	private Matrix4f createInitialPose() {
		var rotation = getDirection().getRotation();
		var visualPosition = getVisualPosition();
		return new Matrix4f().translate(visualPosition.getX(), visualPosition.getY(), visualPosition.getZ())
				.translate(0.5f, 0.5f, 0.5f)
				.scale(0.9995f)
				.rotate(rotation)
				.scale(1, -1, -1)
				.translate(0, -1, 0);
	}

	private Direction getDirection() {
		if (blockState.getBlock() instanceof ShulkerBoxBlock) {
			return blockState.getValue(ShulkerBoxBlock.FACING);
		}

		return Direction.UP;
	}

	@Override
	public void beginFrame(Context context) {
		if (doDistanceLimitThisFrame(context) || !isVisible(context.frustum())) {
			return;
		}

		float progress = blockEntity.getProgress(context.partialTick());
		if (progress == lastProgress) {
			return;
		}
		lastProgress = progress;

		lid.yRot(1.5f * Mth.PI * progress);
		lid.yPos(24f - progress * 8f);

		instances.updateInstancesStatic(initialPose);
	}

	@Override
	public void updateLight(float partialTick) {
		int packedLight = computePackedLight();
		instances.traverse(instance -> {
			instance.light(packedLight)
					.setChanged();
		});
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		instances.traverse(consumer);
	}

	@Override
	protected void _delete() {
		instances.delete();
	}
}
