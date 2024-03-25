package com.jozufozu.flywheel.vanilla.effect;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.Effect;
import com.jozufozu.flywheel.api.visual.EffectVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.task.ForEachPlan;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

// http://www.kfish.org/boids/pseudocode.html
public class ExampleEffect implements Effect {
	private static final List<ExampleEffect> ALL_EFFECTS = new ArrayList<>();

	private static final int VISUAL_COUNT = 500;
	private static final float SPAWN_RADIUS = 8.0f;
	private static final float LIMIT_RANGE = 10.0f;
	private static final float SPEED_LIMIT = 0.1f;
	private static final float RENDER_SCALE = 2 / 16f;

	private static final float SIGHT_RANGE = 5;

	private static final float COHERENCE = 1f / 60f;
	private static final float SEPARATION = 0.05f;
	private static final float ALIGNMENT = 1 / 20f;
	private static final float TENDENCY = 1 / 1000f;
	private static final float AVERSION = 1;

	private static final float GNAT_JITTER = 0.05f;

	private final Level level;
	private final Vector3f targetPoint;

	public ExampleEffect(Level level, Vector3f targetPoint) {
		this.level = level;
		this.targetPoint = targetPoint;
	}

	public static void tick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START || Minecraft.getInstance().isPaused()) {
			return;
		}

		trySpawnNewEffect();
	}

	public static void onReloadLevelRenderer(ReloadLevelRendererEvent event) {
		ALL_EFFECTS.clear();
	}

	private static void trySpawnNewEffect() {
		Level level = Minecraft.getInstance().level;
		Player player = Minecraft.getInstance().player;

		if (player == null || level == null) {
			return;
		}

		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		if (!ALL_EFFECTS.isEmpty() && level.random.nextFloat() > 0.005f) {
			return;
		}

		Vec3 playerPos = player.position();

		var x = (float) (playerPos.x + Mth.nextFloat(level.random, -20, 20));
		var y = (float) (playerPos.y + Mth.nextFloat(level.random, 0, 5));
		var z = (float) (playerPos.z + Mth.nextFloat(level.random, -20, 20));

		ExampleEffect effect = new ExampleEffect(level, new Vector3f(x, y, z));
		ALL_EFFECTS.add(effect);
		manager.getEffects().queueAdd(effect);
	}

	@Override
	public EffectVisual<?> visualize(VisualizationContext ctx) {
		return new ExampleVisual(ctx);
	}

	public class ExampleVisual implements EffectVisual<ExampleEffect>, TickableVisual, DynamicVisual {
		private final List<BoidVisual> effects;
		private final List<Boid> boids;

		public ExampleVisual(VisualizationContext ctx) {
			this.effects = new ArrayList<>(VISUAL_COUNT);
			this.boids = new ArrayList<>(VISUAL_COUNT);

			for (int i = 0; i < VISUAL_COUNT; i++) {
				var x = targetPoint.x + Mth.nextFloat(level.random, -SPAWN_RADIUS, SPAWN_RADIUS);
				var y = targetPoint.y + Mth.nextFloat(level.random, -SPAWN_RADIUS, SPAWN_RADIUS);
				var z = targetPoint.z + Mth.nextFloat(level.random, -SPAWN_RADIUS, SPAWN_RADIUS);

				Boid boid = new Boid(x, y, z);
				boids.add(boid);
				effects.add(new BoidVisual(ctx, boid));
			}
		}

		@Override
		public Plan<VisualTickContext> planTick() {
			Plan<VisualTickContext> beginTick = ForEachPlan.of(() -> boids, Boid::beginTick);
			return beginTick.then(ForEachPlan.of(() -> effects, boid -> boid.self.tick(boids)));
		}

		@Override
		public Plan<VisualFrameContext> planFrame() {
			return ForEachPlan.of(() -> effects, BoidVisual::beginFrame);
		}

		@Override
		public void init(float partialTick) {
		}

		@Override
		public void update(float partialTick) {
		}

        @Override
		public void delete() {
			effects.forEach(BoidVisual::_delete);
		}
	}

	public static class Boid {
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

		public void tick(List<Boid> swarm) {
			int seen = 0;
			coherence.set(0);
			alignment.set(0);
			for (Boid boid : swarm) {
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

	public static class BoidVisual {
		private final Boid self;
		private final Vec3i renderOrigin;

		private final TransformedInstance instance;

		public BoidVisual(VisualizationContext ctx, Boid self) {
			renderOrigin = ctx.renderOrigin();
			this.self = self;

			instance = ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, Models.block(Blocks.SHROOMLIGHT.defaultBlockState()))
					.createInstance();

			instance.setBlockLight(15)
					.setSkyLight(15);
		}

		public void _delete() {
			instance.delete();
		}

		public void beginFrame(VisualFrameContext context) {
			float partialTick = context.partialTick();
			var x = Mth.lerp(partialTick, self.lastPosition.x, self.position.x);
			var y = Mth.lerp(partialTick, self.lastPosition.y, self.position.y);
			var z = Mth.lerp(partialTick, self.lastPosition.z, self.position.z);

			instance.loadIdentity()
					.translateBack(renderOrigin)
					.translate(x, y, z)
					.scale(RENDER_SCALE);
		}
	}
}
