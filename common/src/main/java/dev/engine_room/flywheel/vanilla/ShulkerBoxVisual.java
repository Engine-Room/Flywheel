package dev.engine_room.flywheel.vanilla;

import java.util.function.Consumer;

import org.joml.Quaternionf;

import com.mojang.math.Axis;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.ModelCache;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.model.part.ModelPartConverter;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.util.RecyclingPoseStack;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
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

	private static final ModelCache<Material> BASE_MODELS = new ModelCache<>(texture -> {
		return new SingleMeshModel(ModelPartConverter.convert(ModelLayers.SHULKER, texture.sprite(), "base"), MATERIAL);
	});
	private static final ModelCache<Material> LID_MODELS = new ModelCache<>(texture -> {
		return new SingleMeshModel(ModelPartConverter.convert(ModelLayers.SHULKER, texture.sprite(), "lid"), MATERIAL);
	});

	private final TransformedInstance base;
	private final TransformedInstance lid;

	private final RecyclingPoseStack stack = new RecyclingPoseStack();

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

		var rotation = getDirection().getRotation();

		stack.setIdentity();
		TransformStack.of(stack)
				.translate(getVisualPosition())
				.translate(0.5f)
				.scale(0.9995f)
				.rotate(rotation)
				.scale(1, -1, -1)
				.translateY(-1);

		base = createBaseInstance(texture).setTransform(stack);
		base.setChanged();
		lid = createLidInstance(texture).setTransform(stack);
		lid.setChanged();
	}

	private TransformedInstance createBaseInstance(Material texture) {
		return instancerProvider().instancer(InstanceTypes.TRANSFORMED, BASE_MODELS.get(texture))
				.createInstance();
	}

	private TransformedInstance createLidInstance(Material texture) {
		return instancerProvider().instancer(InstanceTypes.TRANSFORMED, LID_MODELS.get(texture))
				.createInstance();
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

		Quaternionf spin = Axis.YP.rotationDegrees(270.0f * progress);

		TransformStack.of(stack)
				.pushPose()
				.translateY(-progress * 0.5f)
				.rotate(spin);

		lid.setTransform(stack)
				.setChanged();

		stack.popPose();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(base, lid);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(base);
		consumer.accept(lid);
	}

	@Override
	protected void _delete() {
		base.delete();
		lid.delete();
	}
}
