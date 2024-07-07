package dev.engine_room.flywheel.impl.visualization.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.LitVisual;
import dev.engine_room.flywheel.api.visual.SmoothLitVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visual.Visual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.task.ForEachPlan;
import dev.engine_room.flywheel.lib.task.NestedPlan;
import dev.engine_room.flywheel.lib.task.PlanMap;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public abstract class Storage<T> {
	protected final Supplier<VisualizationContext> visualizationContextSupplier;
	protected final PlanMap<DynamicVisual, DynamicVisual.Context> dynamicVisuals = new PlanMap<>();
	protected final PlanMap<TickableVisual, TickableVisual.Context> tickableVisuals = new PlanMap<>();
	protected final List<SimpleDynamicVisual> simpleDynamicVisuals = new ArrayList<>();
	protected final List<SimpleTickableVisual> simpleTickableVisuals = new ArrayList<>();
	protected final LitVisualStorage litVisuals = new LitVisualStorage();
	protected final SmoothLitVisualStorage smoothLitVisuals = new SmoothLitVisualStorage();

	private final Map<T, Visual> visuals = new Reference2ObjectOpenHashMap<>();

	public Storage(Supplier<VisualizationContext> visualizationContextSupplier) {
		this.visualizationContextSupplier = visualizationContextSupplier;
	}

	public Collection<Visual> getAllVisuals() {
		return visuals.values();
	}

	public void add(T obj, float partialTick) {
		Visual visual = visuals.get(obj);

		if (visual == null) {
			create(obj, partialTick);
		}
	}

	public void remove(T obj) {
		Visual visual = visuals.remove(obj);

		if (visual == null) {
			return;
		}

		if (visual instanceof TickableVisual tickable) {
			if (visual instanceof SimpleTickableVisual simpleTickable) {
				simpleTickableVisuals.remove(simpleTickable);
			} else {
				tickableVisuals.remove(tickable);
			}
		}
		if (visual instanceof DynamicVisual dynamic) {
			if (visual instanceof SimpleDynamicVisual simpleDynamic) {
				simpleDynamicVisuals.remove(simpleDynamic);
			} else {
				dynamicVisuals.remove(dynamic);
			}
		}
		if (visual instanceof LitVisual lit) {
			litVisuals.remove(lit);
		}
		if (visual instanceof SmoothLitVisual smoothLit) {
			smoothLitVisuals.remove(smoothLit);
		}
		visual.delete();
	}

	public void update(T obj, float partialTick) {
		Visual visual = visuals.get(obj);

		if (visual == null) {
			return;
		}

		visual.update(partialTick);
	}

	public void recreateAll(float partialTick) {
		tickableVisuals.clear();
		dynamicVisuals.clear();
		simpleTickableVisuals.clear();
		simpleDynamicVisuals.clear();
		litVisuals.clear();
		smoothLitVisuals.clear();
		visuals.replaceAll((obj, visual) -> {
			visual.delete();

			var out = createRaw(obj, partialTick);

			if (out != null) {
				setup(out);
			}

			return out;
		});
	}

	public void invalidate() {
		tickableVisuals.clear();
		dynamicVisuals.clear();
		litVisuals.clear();
		visuals.values()
				.forEach(Visual::delete);
		visuals.clear();
	}

	private void create(T obj, float partialTick) {
		var visual = createRaw(obj, partialTick);

		if (visual != null) {
			setup(visual);
			visuals.put(obj, visual);
		}
	}

	@Nullable
	protected abstract Visual createRaw(T obj, float partialTick);

	public Plan<DynamicVisual.Context> framePlan() {
		return NestedPlan.of(dynamicVisuals, litVisuals.plan(), ForEachPlan.of(() -> simpleDynamicVisuals, SimpleDynamicVisual::beginFrame));
	}

	public Plan<TickableVisual.Context> tickPlan() {
		return NestedPlan.of(tickableVisuals, ForEachPlan.of(() -> simpleTickableVisuals, SimpleTickableVisual::tick));
	}

	public void enqueueLightUpdateSection(long section) {
		litVisuals.enqueueLightUpdateSection(section);
	}

	private void setup(Visual visual) {
		if (visual instanceof TickableVisual tickable) {
			if (visual instanceof SimpleTickableVisual simpleTickable) {
				simpleTickableVisuals.add(simpleTickable);
			} else {
				tickableVisuals.add(tickable, tickable.planTick());
			}
		}

		if (visual instanceof DynamicVisual dynamic) {
			if (visual instanceof SimpleDynamicVisual simpleDynamic) {
				simpleDynamicVisuals.add(simpleDynamic);
			} else {
				dynamicVisuals.add(dynamic, dynamic.planFrame());
			}
		}

		if (visual instanceof LitVisual lit) {
			litVisuals.setNotifierAndAdd(lit);
		}

		if (visual instanceof SmoothLitVisual smoothLit) {
			smoothLitVisuals.add(smoothLit);
		}
	}

	/**
	 * Is the given object currently capable of being added?
	 *
	 * @return true if the object is currently capable of being visualized.
	 */
	public abstract boolean willAccept(T obj);

	public LongSet lightSections() {
		return smoothLitVisuals.sections();
	}
}
