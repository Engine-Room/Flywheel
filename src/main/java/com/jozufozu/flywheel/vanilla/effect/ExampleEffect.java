package com.jozufozu.flywheel.vanilla.effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.joml.FrustumIntersection;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.instance.effect.Effect;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.lib.box.ImmutableBox;
import com.jozufozu.flywheel.lib.box.MutableBox;
import com.jozufozu.flywheel.lib.instance.AbstractInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.struct.StructTypes;
import com.jozufozu.flywheel.lib.struct.TransformedPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

// http://www.kfish.org/boids/pseudocode.html
public class ExampleEffect implements Effect {

	private static final List<ExampleEffect> ALL_EFFECTS = new ArrayList<>();

	private static final int INSTANCE_COUNT = 500;
	private static final float SPAWN_RADIUS = 8.0f;
	private static final float LIMIT_RANGE = 10.0f;
	private static final float SPEED_LIMIT = 0.1f;
	private static final float RENDER_SCALE = 1 / 16f;

	private static final float SIGHT_RANGE = 5;


	private static final float COHERENCE = 1f / 60f;
	private static final float SEPARATION = 0.05f;
	private static final float ALIGNMENT = 1 / 20f;
	private static final float TENDENCY = 1 / 1000f;
	private static final float AVERSION = 1;

	private static final float GNAT_JITTER = 0.05f;

	private final Level level;
	private final Vector3f targetPoint;
	private final BlockPos blockPos;
	private final ImmutableBox volume;

	private final List<Instance> effects;

	private final List<Boid> boids;

	public ExampleEffect(Level level, Vector3f targetPoint) {
		this.level = level;
		this.targetPoint = targetPoint;
		this.blockPos = new BlockPos(targetPoint.x, targetPoint.y, targetPoint.z);
		this.volume = MutableBox.from(this.blockPos);
		this.effects = new ArrayList<>(INSTANCE_COUNT);
		this.boids = new ArrayList<>(INSTANCE_COUNT);
	}

	public static void tick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END || Minecraft.getInstance().isPaused()) {
			return;
		}

		trySpawnNewEffect();
	}

	public static void onReload(ReloadRenderersEvent event) {
		ALL_EFFECTS.clear();
	}

	private static void trySpawnNewEffect() {
		Level level = Minecraft.getInstance().level;
		Player player = Minecraft.getInstance().player;

		if (player == null || level == null) {
			return;
		}

		if (!ALL_EFFECTS.isEmpty() && level.random.nextFloat() > 0.005f) {
			return;
		}

		Vec3 playerPos = player.position();

		var x = (float) (playerPos.x + level.random.nextFloat(-20, 20));
		var y = (float) (playerPos.y + level.random.nextFloat(0, 5));
		var z = (float) (playerPos.z + level.random.nextFloat(-20, 20));

		ExampleEffect effect = new ExampleEffect(level, new Vector3f(x, y, z));
		ALL_EFFECTS.add(effect);
		InstancedRenderDispatcher.getEffects(level)
				.queueAdd(effect);
	}

	@Override
	public Collection<com.jozufozu.flywheel.api.instance.Instance> createInstances(InstancerProvider instancerManager) {
		effects.clear();
		boids.clear();
		for (int i = 0; i < INSTANCE_COUNT; i++) {
			var x = targetPoint.x + level.random.nextFloat(-SPAWN_RADIUS, SPAWN_RADIUS);
			var y = targetPoint.y + level.random.nextFloat(-SPAWN_RADIUS, SPAWN_RADIUS);
			var z = targetPoint.z + level.random.nextFloat(-SPAWN_RADIUS, SPAWN_RADIUS);

			Boid boid = new Boid(x, y, z);
			boids.add(boid);
			effects.add(new Instance(instancerManager, level, boid));
		}
		return Collections.unmodifiableList(effects);
	}

	public class Boid {
		final Vector3f lastPosition;
		final Vector3f position;
		final Vector3f lastVelocity = new Vector3f(0);
		final Vector3f velocity = new Vector3f(0);

		final Vector3f scratch = new Vector3f(0);
		final Vector3f coherence = new Vector3f(0);
		final Vector3f alignment = new Vector3f(0);

		public Boid(float x, float y, float z) {
			lastPosition = new Vector3f(x, y, z);
			position = new Vector3f(x, y, z);
		}


		private void beginTick() {
			lastVelocity.set(velocity);
			lastPosition.set(position);
		}

		public void tick() {
			beginTick();

			int seen = 0;
			coherence.set(0);
			alignment.set(0);
			for (Boid boid : boids) {
				if (boid == this) {
					continue;
				}

				float distance = boid.lastPosition.distance(lastPosition);

				if (distance > SIGHT_RANGE) {
					continue;
				}
				seen++;

				coherence(boid);
				separation(boid);
				alignment(boid);
			}

			if (seen > 0) {
				coherencePost(seen);
				alignmentPost(seen);
			}
			//tend(ExampleEffect.this.targetPoint);

			avoidPlayer();

			position.add(capSpeed(velocity));
		}

		private void avoidPlayer() {
			var player = Minecraft.getInstance().player.position();
			scratch.set(player.x, player.y, player.z);

			float dsq = lastPosition.distanceSquared(scratch);
			if (dsq > SIGHT_RANGE * SIGHT_RANGE) {
				return;
			}

			lastPosition.sub(scratch, scratch)
					.mul(AVERSION / dsq);

			velocity.add(capSpeed(scratch));
		}

		private void coherence(Boid other) {
			this.coherence.add(other.lastPosition);
		}

		private void separation(Boid other) {
			float dsq = lastPosition.distanceSquared(other.lastPosition);
			var push = other.lastPosition.sub(lastPosition, this.scratch)
					.mul(SEPARATION / dsq);

			this.velocity.sub(push);
		}

		private void alignment(Boid boid) {
			this.alignment.add(boid.lastVelocity);
		}

		private void coherencePost(int seen) {
			this.coherence.div(seen)
					.sub(lastPosition)
					.mul(COHERENCE);
			this.velocity.add(capSpeed(this.coherence));
		}

		private void alignmentPost(int seen) {
			this.alignment.div(seen)
					.sub(lastVelocity)
					.mul(ALIGNMENT);

			this.velocity.add(this.alignment);
		}

		private void tend(Vector3f target) {
			this.scratch.set(target)
					.sub(lastPosition)
					.mul(TENDENCY);
			this.velocity.add(capSpeed(this.scratch));
		}

		private static Vector3f capSpeed(Vector3f vec) {
			return vec.normalize(SPEED_LIMIT);
		}
	}

	public class Instance extends AbstractInstance implements DynamicInstance, TickableInstance {

		private final Boid self;
		TransformedPart instance;

		public Instance(InstancerProvider instancerManager, Level level, Boid self) {
			super(instancerManager, level);
			this.self = self;
		}

		@Override
		public void init() {
			instance = instancerManager.getInstancer(StructTypes.TRANSFORMED, Models.block(Blocks.SHROOMLIGHT.defaultBlockState()), RenderStage.AFTER_PARTICLES)
					.createInstance();

			instance.setBlockLight(15)
					.setSkyLight(15);
		}

		@Override
		public BlockPos getWorldPosition() {
			return blockPos;
		}

		@Override
		protected void _delete() {
			instance.delete();
		}

		@Override
		public ImmutableBox getVolume() {
			return volume;
		}

		@Override
		public void tick() {
			self.tick();
		}

		@Override
		public void beginFrame() {
			float partialTicks = AnimationTickHolder.getPartialTicks();

			var x = Mth.lerp(partialTicks, self.lastPosition.x, self.position.x);
			var y = Mth.lerp(partialTicks, self.lastPosition.y, self.position.y);
			var z = Mth.lerp(partialTicks, self.lastPosition.z, self.position.z);

			instance.loadIdentity()
					.translateBack(instancerManager.getOriginCoordinate())
					.translate(x, y, z)
					.scale(RENDER_SCALE);
		}

		@Override
		public boolean decreaseTickRateWithDistance() {
			return false;
		}

		@Override
		public boolean decreaseFramerateWithDistance() {
			return false;
		}

		@Override
		public boolean checkFrustum(FrustumIntersection frustum) {
			return true;
		}
	}
}
