package com.jozufozu.flywheel.vanilla;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.hardcoded.ModelPart;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.jozufozu.flywheel.util.AnimationTickHolder;

import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

public class BellInstance extends BlockEntityInstance<BellBlockEntity> implements DynamicInstance {

	private final OrientedData bell;

	private float lastRingTime = Float.NaN;

	public BellInstance(MaterialManager materialManager, BellBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		bell = createBellInstance()
				.setPivot(0.5f, 0.75f, 0.5f)
				.setPosition(getInstancePosition());
	}

	@Override
	public void beginFrame() {
		float ringTime = (float)blockEntity.ticks + AnimationTickHolder.getPartialTicks();

		if (ringTime == lastRingTime) return;
		lastRingTime = ringTime;

		if (blockEntity.shaking) {
			float angle = Mth.sin(ringTime / (float) Math.PI) / (4.0F + ringTime / 3.0F);

			Vector3f ringAxis = blockEntity.clickDirection.getCounterClockWise().step();

			bell.setRotation(new Quaternionf().rotationAxis(angle, ringAxis));
		} else {
			bell.setRotation(new Quaternionf());
		}
	}

	@Override
	public void updateLight() {
		relight(getWorldPosition(), bell);
	}

	@Override
	public void remove() {
		bell.delete();
	}

	private OrientedData createBellInstance() {
        return materialManager.defaultCutout()
                .material(Materials.ORIENTED)
				.model(blockEntity.getType(), BellInstance::createBellModel)
				.createInstance();
	}

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
