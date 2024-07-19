package dev.engine_room.flywheel.vanilla;

import java.util.function.Consumer;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.model.ModelHolder;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.model.part.ModelPartConverter;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

public class BellVisual extends AbstractBlockEntityVisual<BellBlockEntity> implements SimpleDynamicVisual {
	private static final ModelHolder BELL_MODEL = new ModelHolder(() -> {
		return new SingleMeshModel(ModelPartConverter.convert(ModelLayers.BELL, BellRenderer.BELL_RESOURCE_LOCATION.sprite(), "bell_body"), Materials.BELL);
	});

	private final OrientedInstance bell;

	private boolean wasShaking = false;

	public BellVisual(VisualizationContext ctx, BellBlockEntity blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		bell = createBellInstance().setPivot(0.5f, 0.75f, 0.5f)
				.setPosition(getVisualPosition());
		bell.setChanged();

		updateRotation(partialTick);
	}

	private OrientedInstance createBellInstance() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, BELL_MODEL.get())
				.createInstance();
	}

	@Override
	public void beginFrame(Context context) {
		if (doDistanceLimitThisFrame(context) || !isVisible(context.frustum())) {
			return;
		}

		updateRotation(context.partialTick());
	}

	private void updateRotation(float partialTick) {
		if (blockEntity.shaking) {
			float ringTime = (float) blockEntity.ticks + partialTick;
			float angle = Mth.sin(ringTime / (float) Math.PI) / (4.0F + ringTime / 3.0F);

			Vector3f ringAxis = blockEntity.clickDirection.getCounterClockWise()
					.step();

			bell.setRotation(new Quaternionf(new AxisAngle4f(angle, ringAxis)))
					.setChanged();

			wasShaking = true;
		} else if (wasShaking) {
			bell.setRotation(new Quaternionf())
					.setChanged();

			wasShaking = false;
		}
	}

	@Override
	public void updateLight(float partialTick) {
		relight(bell);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(bell);
	}

	@Override
	protected void _delete() {
		bell.delete();
	}
}
