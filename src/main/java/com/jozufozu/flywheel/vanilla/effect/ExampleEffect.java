package com.jozufozu.flywheel.vanilla.effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jozufozu.flywheel.api.InstancerManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.effect.Effect;
import com.jozufozu.flywheel.core.Models;
import com.jozufozu.flywheel.core.structs.StructTypes;
import com.jozufozu.flywheel.core.structs.model.TransformedPart;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

public class ExampleEffect implements Effect {

	private static final int INSTANCE_COUNT = 50;

	private final Level level;
	private final Vec3 targetPoint;
	private final BlockPos blockPos;
	private final ImmutableBox volume;

	private final List<Instance> effects;

	public ExampleEffect(Level level, Vec3 targetPoint) {
		this.level = level;
		this.targetPoint = targetPoint;
		this.blockPos = new BlockPos(targetPoint);
		this.effects = new ArrayList<>();
		this.volume = GridAlignedBB.from(this.blockPos);
	}

	public static void spawn(TickEvent.PlayerTickEvent event) {
		if (event.side == LogicalSide.SERVER || event.phase == TickEvent.Phase.START) {
			return;
		}

		Player player = event.player;
		Level level = player.level;

		if (level.random.nextFloat() > 0.01) {
			return;
		}

		var effects = InstancedRenderDispatcher.getEffects(level);

		effects.add(new ExampleEffect(level, player.position()));
	}

	@Override
	public Collection<? extends AbstractInstance> createInstances(InstancerManager instancerManager) {
		effects.clear();
		for (int i = 0; i < INSTANCE_COUNT; i++) {
			effects.add(new Instance(instancerManager, level));
		}
		return effects;
	}

	public class Instance extends AbstractInstance implements DynamicInstance {

		TransformedPart firefly;

		public Instance(InstancerManager instancerManager, Level level) {
			super(instancerManager, level);
		}

		@Override
		public void init() {
			firefly = instancerManager.factory(StructTypes.TRANSFORMED)
					.model(Models.block(Blocks.SHROOMLIGHT.defaultBlockState()))
					.createInstance();

			firefly.setBlockLight(15)
					.setSkyLight(15);
		}

		@Override
		public BlockPos getWorldPosition() {
			return blockPos;
		}

		@Override
		public void remove() {
			firefly.delete();
		}

		@Override
		public ImmutableBox getVolume() {
			return volume;
		}

		@Override
		public void beginFrame() {
			var x = level.random.nextFloat() * 3 - 1.5;
			var y = level.random.nextFloat() * 3 - 1.5;
			var z = level.random.nextFloat() * 3 - 1.5;

			firefly.loadIdentity()
					.translate(instancerManager.getOriginCoordinate())
					.translate(targetPoint)
					.translate(x, y, z)
					.scale(1 / 16f);
		}
	}
}
