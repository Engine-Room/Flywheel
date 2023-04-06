package com.jozufozu.flywheel.vanilla;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.lib.instance.AbstractBlockEntityInstance;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.model.SimpleLazyModel;
import com.jozufozu.flywheel.lib.modelpart.ModelPart;
import com.jozufozu.flywheel.lib.struct.OrientedPart;
import com.jozufozu.flywheel.lib.struct.StructTypes;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

public class BellInstance extends AbstractBlockEntityInstance<BellBlockEntity> implements DynamicInstance {
	private static final SimpleLazyModel MODEL = new SimpleLazyModel(BellInstance::createBellModel, Materials.BELL);

	private OrientedPart bell;

	private float lastRingTime = Float.NaN;

	public BellInstance(InstanceContext ctx, BellBlockEntity blockEntity) {
		super(ctx, blockEntity);
	}

	@Override
	public void init() {
		bell = createBellInstance().setPivot(0.5f, 0.75f, 0.5f)
				.setPosition(getInstancePosition());

		super.init();
	}

	@Override
	public void beginFrame() {
		float ringTime = (float) blockEntity.ticks + AnimationTickHolder.getPartialTicks();

		if (ringTime == lastRingTime) {
			return;
		}
		lastRingTime = ringTime;

		if (blockEntity.shaking) {
			float angle = Mth.sin(ringTime / (float) Math.PI) / (4.0F + ringTime / 3.0F);

			Vector3f ringAxis = blockEntity.clickDirection.getCounterClockWise()
					.step();

			bell.setRotation(ringAxis.rotation(angle));
		} else {
			bell.setRotation(Quaternion.ONE);
		}
	}

	@Override
	public void updateLight() {
		relight(pos, bell);
	}

	@Override
	public List<InstancedPart> getCrumblingParts() {
		return List.of(bell);
	}

	@Override
	protected void _delete() {
		bell.delete();
	}

	private OrientedPart createBellInstance() {
		return instancerProvider.instancer(StructTypes.ORIENTED, MODEL, RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
	}

	@NotNull
	private static ModelPart createBellModel() {
		return ModelPart.builder("bell", 32, 32)
				.sprite(BellRenderer.BELL_RESOURCE_LOCATION.sprite())
				.cuboid()
				.start(5.0F, 6.0F, 5.0F)
				.size(6.0F, 7.0F, 6.0F)
				.endCuboid()
				.cuboid()
				.textureOffset(0, 13)
				.start(4.0F, 4.0F, 4.0F)
				.size(8.0F, 2.0F, 8.0F)
				.endCuboid()
				.build();
	}
}
