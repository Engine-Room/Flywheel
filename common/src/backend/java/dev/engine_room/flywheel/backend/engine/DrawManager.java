package dev.engine_room.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.backend.FlwBackend;
import dev.engine_room.flywheel.backend.engine.embed.Environment;
import dev.engine_room.flywheel.lib.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.ModelBakery;

public abstract class DrawManager<N extends AbstractInstancer<?>> {
	/**
	 * A map of instancer keys to instancers.
	 * <br>
	 * This map is populated as instancers are requested and contains both initialized and uninitialized instancers.
	 */
	protected final Map<InstancerKey<?>, N> instancers = new ConcurrentHashMap<>();
	/**
	 * A list of instancers that have not yet been initialized.
	 * <br>
	 * All new instancers land here before having resources allocated in {@link #flush}.
	 */
	protected final Queue<UninitializedInstancer<N, ?>> initializationQueue = new ConcurrentLinkedQueue<>();

	@SuppressWarnings("unchecked")
	public <I extends Instance> Instancer<I> getInstancer(Environment environment, InstanceType<I> type, Model model, RenderStage stage) {
		return (Instancer<I>) instancers.computeIfAbsent(new InstancerKey<>(environment, type, model, stage), this::createAndDeferInit);
	}

	public void delete() {
		instancers.clear();
		initializationQueue.clear();
	}

	public void flush(LightStorage lightStorage) {
		// Thread safety: flush is called from the render thread after all visual updates have been made,
		// so there are no:tm: threads we could be racing with.
		for (var instancer : initializationQueue) {
			initialize(instancer.key(), instancer.instancer());
		}
		initializationQueue.clear();
	}

	public void onRenderOriginChanged() {
		instancers.values()
				.forEach(AbstractInstancer::clear);
	}

	public abstract void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks);

	public abstract void renderStage(RenderStage stage);

	protected abstract <I extends Instance> N create(InstancerKey<I> type);

	protected abstract <I extends Instance> void initialize(InstancerKey<I> key, N instancer);

	private N createAndDeferInit(InstancerKey<?> key) {
		var out = create(key);

		// Only queue the instancer for initialization if it has anything to render.
		if (checkAndWarnEmptyModel(key.model())) {
			// Thread safety: this method is called atomically from within computeIfAbsent,
			// so you'd think we don't need extra synchronization to protect the queue, but
			// somehow threads can race here and wind up never initializing an instancer.
			initializationQueue.add(new UninitializedInstancer<>(key, out));
		}
		return out;
	}

	protected record UninitializedInstancer<N, I extends Instance>(InstancerKey<I> key, N instancer) {

	}

	private static boolean checkAndWarnEmptyModel(Model model) {
		if (!model.meshes().isEmpty()) {
			return true;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Creating an instancer for a model with no meshes! Stack trace:");

		StackWalker.getInstance()
				// .walk(s -> s.skip(3)) // this causes forEach to crash for some reason
				.forEach(f -> builder.append("\n\t")
						.append(f.toString()));

		FlwBackend.LOGGER.warn(builder.toString());

		return false;
	}

	protected static <I extends AbstractInstancer<?>> Map<GroupKey<?>, Int2ObjectMap<List<Pair<I, InstanceHandleImpl>>>> doCrumblingSort(Class<I> clazz, List<Engine.CrumblingBlock> crumblingBlocks) {
		Map<GroupKey<?>, Int2ObjectMap<List<Pair<I, InstanceHandleImpl>>>> byType = new HashMap<>();
		for (Engine.CrumblingBlock block : crumblingBlocks) {
			int progress = block.progress();

			if (progress < 0 || progress >= ModelBakery.DESTROY_TYPES.size()) {
				continue;
			}

			for (Instance instance : block.instances()) {
				// Filter out instances that weren't created by this engine.
				// If all is well, we probably shouldn't take the `continue`
				// branches but better to do checked casts.
				if (!(instance.handle() instanceof InstanceHandleImpl impl)) {
					continue;
				}

				AbstractInstancer<?> abstractInstancer = impl.instancer;
				if (!clazz.isInstance(abstractInstancer)) {
					continue;
				}

				var instancer = clazz.cast(abstractInstancer);

				byType.computeIfAbsent(new GroupKey<>(instancer.type, instancer.environment), $ -> new Int2ObjectArrayMap<>())
						.computeIfAbsent(progress, $ -> new ArrayList<>())
						.add(Pair.of(instancer, impl));
			}
		}
		return byType;
	}
}
