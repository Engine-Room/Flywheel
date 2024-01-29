package com.jozufozu.flywheel.impl.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Supplier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.Effect;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.api.visualization.VisualManager;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.api.visualization.VisualizationLevel;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.extension.LevelExtension;
import com.jozufozu.flywheel.impl.task.FlwTaskExecutor;
import com.jozufozu.flywheel.impl.visual.VisualFrameContextImpl;
import com.jozufozu.flywheel.impl.visual.VisualTickContextImpl;
import com.jozufozu.flywheel.impl.visualization.manager.BlockEntityStorage;
import com.jozufozu.flywheel.impl.visualization.manager.EffectStorage;
import com.jozufozu.flywheel.impl.visualization.manager.EntityStorage;
import com.jozufozu.flywheel.impl.visualization.manager.VisualManagerImpl;
import com.jozufozu.flywheel.impl.visualization.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.impl.visualization.ratelimit.DistanceUpdateLimiterImpl;
import com.jozufozu.flywheel.impl.visualization.ratelimit.NonLimiter;
import com.jozufozu.flywheel.lib.task.Flag;
import com.jozufozu.flywheel.lib.task.IfElsePlan;
import com.jozufozu.flywheel.lib.task.MapContextPlan;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.lib.task.RaisePlan;
import com.jozufozu.flywheel.lib.task.SimplePlan;
import com.jozufozu.flywheel.lib.util.LevelAttached;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A manager class for a single world where visualization is supported.
 */
public class VisualizationManagerImpl implements VisualizationManager {
	private static final LevelAttached<VisualizationManagerImpl> MANAGERS = new LevelAttached<>(VisualizationManagerImpl::new, VisualizationManagerImpl::delete);

	private final Engine engine;
	private final TaskExecutor taskExecutor;

	private final VisualManagerImpl<BlockEntity, BlockEntityStorage> blockEntities;
	private final VisualManagerImpl<Entity, EntityStorage> entities;
	private final VisualManagerImpl<Effect, EffectStorage> effects;

	private final Plan<VisualTickContext> tickPlan;
	private final Plan<RenderContext> framePlan;

	private final Flag tickFlag = new NamedFlag("tick");
	private final Flag frameVisualsFlag = new NamedFlag("frameVisualUpdates");
	private final Flag frameFlag = new NamedFlag("frameComplete");

	protected DistanceUpdateLimiterImpl frameLimiter;

	private VisualizationManagerImpl(LevelAccessor level) {
		frameLimiter = createUpdateLimiter();

		engine = BackendManager.getBackend()
				.createEngine(level);
		taskExecutor = FlwTaskExecutor.get();

		Supplier<VisualizationContext> contextSupplier = () -> new VisualizationContextImpl(engine, engine.renderOrigin());

		var blockEntitiesStorage = new BlockEntityStorage(contextSupplier);
		var entitiesStorage = new EntityStorage(contextSupplier);
		var effectsStorage = new EffectStorage(contextSupplier);

		blockEntities = new VisualManagerImpl<>(blockEntitiesStorage);
		entities = new VisualManagerImpl<>(entitiesStorage);
		effects = new VisualManagerImpl<>(effectsStorage);

		tickPlan = NestedPlan.of(blockEntities.tickPlan(), entities.tickPlan(), effects.tickPlan())
				.then(RaisePlan.raise(tickFlag))
				.simplify();

		var recreate = SimplePlan.<RenderContext>of(context -> blockEntitiesStorage.recreateAll(context.partialTick()),
				context -> entitiesStorage.recreateAll(context.partialTick()),
				context -> effectsStorage.recreateAll(context.partialTick()));

		var update = MapContextPlan.map(this::createVisualFrameContext)
				.to(NestedPlan.of(blockEntities.framePlan(), entities.framePlan(), effects.framePlan()));

		framePlan = IfElsePlan.on((RenderContext ctx) -> engine.updateRenderOrigin(ctx.camera()))
				.ifTrue(recreate)
				.ifFalse(update)
				.plan()
				.then(RaisePlan.raise(frameVisualsFlag))
				.then(engine.createFramePlan())
				.then(RaisePlan.raise(frameFlag))
				.simplify();

		if (level instanceof Level l) {
			LevelExtension.getAllLoadedEntities(l)
					.forEach(entities::queueAdd);
		}
	}

	private VisualFrameContext createVisualFrameContext(RenderContext ctx) {
		Vec3i renderOrigin = engine.renderOrigin();
		var cameraPos = ctx.camera()
				.getPosition();

		Matrix4f viewProjection = new Matrix4f(ctx.viewProjection());
		viewProjection.translate((float) (renderOrigin.getX() - cameraPos.x), (float) (renderOrigin.getY() - cameraPos.y), (float) (renderOrigin.getZ() - cameraPos.z));
		FrustumIntersection frustum = new FrustumIntersection(viewProjection);

		return new VisualFrameContextImpl(ctx.camera(), frustum, ctx.partialTick(), frameLimiter);
	}

	protected DistanceUpdateLimiterImpl createUpdateLimiter() {
		if (FlwConfig.get()
				.limitUpdates()) {
			return new BandedPrimeLimiter();
		} else {
			return new NonLimiter();
		}
	}

	@Contract("null -> false")
	public static boolean supportsVisualization(@Nullable LevelAccessor level) {
		if (!BackendManager.isBackendOn()) {
			return false;
		}

		if (level == null) {
			return false;
		}

		if (!level.isClientSide()) {
			return false;
		}

		if (level instanceof VisualizationLevel flywheelLevel && flywheelLevel.supportsVisualization()) {
			return true;
		}

		return level == Minecraft.getInstance().level;
	}

	@Nullable
	public static VisualizationManagerImpl get(@Nullable LevelAccessor level) {
		if (!supportsVisualization(level)) {
			return null;
		}

		return MANAGERS.get(level);
	}

	public static VisualizationManagerImpl getOrThrow(@Nullable LevelAccessor level) {
		if (!supportsVisualization(level)) {
			throw new IllegalStateException("Cannot retrieve visualization manager when visualization is not supported by level '" + level + "'!");
		}

		return MANAGERS.get(level);
	}

	// TODO: Consider making these reset actions reuse the existing game objects instead of re-adding them
	// potentially by keeping the same VisualizationManagerImpl and deleting the engine and visuals but not the game objects
	public static void reset(LevelAccessor level) {
		MANAGERS.remove(level);
	}

	public static void resetAll() {
		MANAGERS.reset();
	}

	@Override
	public Vec3i getRenderOrigin() {
		return engine.renderOrigin();
	}

	@Override
	public VisualManager<BlockEntity> getBlockEntities() {
		return blockEntities;
	}

	@Override
	public VisualManager<Entity> getEntities() {
		return entities;
	}

	@Override
	public VisualManager<Effect> getEffects() {
		return effects;
	}

	/**
	 * Tick the visuals after the game has ticked:
	 * <p>
	 * Call {@link TickableVisual#tick} on all visuals in this world.
	 * </p>
	 */
	public void tick() {
		// Make sure we're done with any prior frame or tick to avoid racing.
		taskExecutor.syncUntil(frameFlag::isRaised);
		frameFlag.lower();

		taskExecutor.syncUntil(tickFlag::isRaised);
		tickFlag.lower();

		tickPlan.execute(taskExecutor, new VisualTickContextImpl());
	}

	/**
	 * Get ready to render a frame.
	 *
	 * <p>Check and update the render origin.
	 * <br>
	 * Call {@link DynamicVisual#beginFrame} on all visuals in this world.</p>
	 */
	public void beginFrame(RenderContext context) {
		// Make sure we're done with the last tick.
		// Note we don't lower here because many frames may happen per tick.
		taskExecutor.syncUntil(tickFlag::isRaised);

		frameVisualsFlag.lower();
		frameFlag.lower();

		frameLimiter.tick();

		framePlan.execute(taskExecutor, context);
	}

	/**
	 * Draw all visuals for the given stage.
	 */
	public void renderStage(RenderContext context, RenderStage stage) {
		engine.renderStage(taskExecutor, context, stage);
	}

	public void renderCrumbling(RenderContext context, Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress) {
		if (destructionProgress.isEmpty()) {
			return;
		}

		taskExecutor.syncUntil(frameVisualsFlag::isRaised);

		List<Engine.CrumblingBlock> crumblingBlocks = new ArrayList<>();

		for (var entry : destructionProgress.long2ObjectEntrySet()) {
			var set = entry.getValue();
			if (set == null || set.isEmpty()) {
				// Nothing to do if there's no crumbling.
				continue;
			}

			var visual = blockEntities.getStorage()
					.visualAtPos(entry.getLongKey());

			if (visual == null) {
				// The block doesn't have a visual, this is probably the common case.
				continue;
			}

			List<Instance> instances = new ArrayList<>();

			visual.collectCrumblingInstances(instance -> {
				if (instance != null) {
					instances.add(instance);
				}
			});

			if (instances.isEmpty()) {
				// The visual doesn't want to render anything crumbling.
				continue;
			}

			var maxDestruction = set.last();

			crumblingBlocks.add(new Engine.CrumblingBlock(maxDestruction.getProgress(), maxDestruction.getPos(), instances));
		}

		if (!crumblingBlocks.isEmpty()) {
			engine.renderCrumbling(taskExecutor, context, crumblingBlocks);
		}
	}

	/**
	 * Free all acquired resources and delete this manager.
	 */
	public void delete() {
		// Just finish everything. This may include the work of others but that's okay.
		taskExecutor.syncPoint();

		// Now clean up.
		blockEntities.invalidate();
		entities.invalidate();
		effects.invalidate();
		engine.delete();
	}

	public void enqueueLightUpdateSections(LongSet sections) {
		blockEntities.getStorage()
				.enqueueLightUpdateSections(sections);
		entities.getStorage()
				.enqueueLightUpdateSections(sections);
		effects.getStorage()
				.enqueueLightUpdateSections(sections);
	}
}
