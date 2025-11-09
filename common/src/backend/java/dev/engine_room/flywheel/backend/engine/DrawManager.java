package dev.engine_room.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.backend.RenderContext;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.backend.FlwBackend;
import dev.engine_room.flywheel.backend.engine.embed.Environment;
import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.engine_room.flywheel.lib.task.ForEachPlan;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.ModelBakery;

public abstract class DrawManager<N extends AbstractInstancer<?>> {
	private static final boolean MODEL_WARNINGS = Boolean.getBoolean("flywheel.modelWarnings");

	/**
	 * A map of instancer keys to instancers.
	 *
	 * <p>This map is populated as instancers are requested and contains both initialized and uninitialized instancers.
	 */
	protected final Map<InstancerKey<?>, N> instancers = new ConcurrentHashMap<>();
	/**
	 * A list of instancers that have not yet been initialized.
	 *
	 * <p>All new instancers land here before having resources allocated in {@link #render}.
	 */
	protected final Queue<UninitializedInstancer<N, ?>> initializationQueue = new ConcurrentLinkedQueue<>();

	/**
	 * Function object to pass into computeIfAbsent.
	 * <p>Create once and cache to avoid allocating every time.
	 */
	protected final Function<InstancerKey<?>, N> createAndDeferInit = this::createAndDeferInit;

	public <I extends Instance> AbstractInstancer<I> getInstancer(Environment environment, InstanceType<I> type, Model model, int bias) {
		return getInstancer(new InstancerKey<>(environment, type, model, bias));
	}

	@SuppressWarnings("unchecked")
	public <I extends Instance> AbstractInstancer<I> getInstancer(InstancerKey<I> key) {
		return (AbstractInstancer<I>) instancers.computeIfAbsent(key, createAndDeferInit);
	}

	public Plan<RenderContext> createFramePlan() {
		// Go wide on instancers to process deletions in parallel.
		return ForEachPlan.of(() -> new ArrayList<>(instancers.values()), AbstractInstancer::parallelUpdate);
	}

	public void render(LightStorage lightStorage, EnvironmentStorage environmentStorage) {
		// Thread safety: flush is called from the render thread after all visual updates have been made,
		// so there are no:tm: threads we could be racing with.
		for (var init : initializationQueue) {
			var instancer = init.instancer();
			if (instancer.instanceCount() > 0) {
				initialize(init.key(), instancer);
			} else {
				instancers.remove(init.key());
			}
		}
		initializationQueue.clear();
	}

	public void onRenderOriginChanged() {
		instancers.values()
				.forEach(AbstractInstancer::clear);
	}

	public abstract void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks);

	protected abstract <I extends Instance> N create(InstancerKey<I> type);

	protected abstract <I extends Instance> void initialize(InstancerKey<I> key, N instancer);

	private N createAndDeferInit(InstancerKey<?> key) {
		var out = create(key);

		// Only queue the instancer for initialization if it has anything to render.
		if (modelHasNoIssues(key.model())) {
			// Thread safety: this method is called atomically from within computeIfAbsent,
			// so you'd think we don't need extra synchronization to protect the queue, but
			// somehow threads can race here and wind up never initializing an instancer.
			initializationQueue.add(new UninitializedInstancer<>(key, out));
		}
		return out;
	}

	private static boolean modelHasNoIssues(Model model) {
		// No point in setting up resources for something that will never be rendered.
		if (model.meshes().isEmpty()) {
			if (MODEL_WARNINGS) {
				StringBuilder builder = new StringBuilder();
				builder.append("Creating an instancer for a model with no meshes! Stack trace:");

				StackWalker.getInstance()
						.forEach(f -> builder.append("\n\t")
								.append(f.toString()));

				FlwBackend.LOGGER.warn(builder.toString());
			}
			return false;
		}

		// Catch nullability issues here so the backend doesn't explode later.
		List<Model.ConfiguredMesh> meshes = model.meshes();
		for (int i = 0; i < meshes.size(); i++) {
			var mesh = meshes.get(i);
			if (!MaterialRenderState.materialIsAllNonNull(mesh.material())) {
				if (MODEL_WARNINGS) {
					StringBuilder builder = new StringBuilder();
					builder.append("ConfiguredMesh at index ")
							.append(i)
							.append(" has null components in its material! Stack trace:");

					StackWalker.getInstance()
							.forEach(f -> builder.append("\n\t")
									.append(f.toString()));

					FlwBackend.LOGGER.warn(builder.toString());
				}
				return false;
			}
		}

		return true;
	}

	@FunctionalInterface
	protected interface State2Instancer<I extends AbstractInstancer<?>> {
		// I tried using a plain Function<State<?>, I> here, but it exploded with type errors.
		@Nullable I apply(InstanceHandleImpl.State<?> state);
	}

	protected static <I extends AbstractInstancer<?>> Map<GroupKey<?>, Int2ObjectMap<List<Pair<I, InstanceHandleImpl<?>>>>> doCrumblingSort(List<Engine.CrumblingBlock> crumblingBlocks, State2Instancer<I> cast) {
		Map<GroupKey<?>, Int2ObjectMap<List<Pair<I, InstanceHandleImpl<?>>>>> byType = new HashMap<>();
		for (Engine.CrumblingBlock block : crumblingBlocks) {
			int progress = block.progress();

			if (progress < 0 || progress >= ModelBakery.DESTROY_TYPES.size()) {
				continue;
			}

			for (Instance instance : block.instances()) {
				// Filter out instances that weren't created by this engine.
				// If all is well, we probably shouldn't take the `continue`
				// branches but better to do checked casts.
				if (!(instance.handle() instanceof InstanceHandleImpl<?> impl)) {
					continue;
				}

				var instancer = cast.apply(impl.state);

				if (instancer == null) {
					continue;
				}

				byType.computeIfAbsent(new GroupKey<>(instancer.type, instancer.environment), $ -> new Int2ObjectArrayMap<>())
						.computeIfAbsent(progress, $ -> new ArrayList<>())
						.add(Pair.of(instancer, impl));
			}
		}
		return byType;
	}

	public void delete() {
		instancers.clear();
		initializationQueue.clear();
	}

	public abstract void triggerFallback();

	public abstract MeshPool meshPool();

	public Map<InstancerKey<?>, N> instancers() {
		return instancers;
	}

	protected record UninitializedInstancer<N, I extends Instance>(InstancerKey<I> key, N instancer) {
	}
}
