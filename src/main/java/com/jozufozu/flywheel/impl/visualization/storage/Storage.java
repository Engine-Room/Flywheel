package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.LitVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.Visual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.lib.visual.SimpleDynamicVisual;
import com.jozufozu.flywheel.lib.visual.SimpleTickableVisual;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public abstract class Storage<T> {
	protected final Supplier<VisualizationContext> visualizationContextSupplier;
	protected final PlanStorage<DynamicVisual, VisualFrameContext> dynamicVisuals = new PlanStorage<>();
	protected final FastPlanStorage<SimpleDynamicVisual, VisualFrameContext> fastDynamicVisuals = new FastPlanStorage<>(SimpleDynamicVisual::beginFrame);
	protected final PlanStorage<TickableVisual, VisualTickContext> tickableVisuals = new PlanStorage<>();
	protected final FastPlanStorage<SimpleTickableVisual, VisualTickContext> fastTickableVisuals = new FastPlanStorage<>(SimpleTickableVisual::tick);
	protected final LitVisualStorage litVisuals = new LitVisualStorage();

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
				fastTickableVisuals.remove(simpleTickable);
			} else {
				tickableVisuals.remove(tickable);
			}
		}
		if (visual instanceof DynamicVisual dynamic) {
			if (visual instanceof SimpleDynamicVisual simpleDynamic) {
				fastDynamicVisuals.remove(simpleDynamic);
			} else {
				dynamicVisuals.remove(dynamic);
			}
		}
		if (visual instanceof LitVisual lit) {
			litVisuals.remove(lit);
		}
		visual.delete();
	}

	public void update(T obj, float partialTick) {
		Visual visual = visuals.get(obj);

		if (visual == null) {
			return;
		}

		// resetting visuals is by default used to handle block state changes.
		if (visual.shouldReset()) {
			// delete and re-create the visual.
			// resetting a visual supersedes updating it.
			remove(obj);
			create(obj, partialTick);
		} else {
			visual.update(partialTick);
		}
	}

	public void recreateAll(float partialTick) {
		tickableVisuals.clear();
		fastTickableVisuals.clear();
		dynamicVisuals.clear();
		fastDynamicVisuals.clear();
		litVisuals.clear();
		visuals.replaceAll((obj, visual) -> {
			visual.delete();

			Visual out = createRaw(obj);

			if (out != null) {
				setup(out, partialTick);
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
		Visual visual = createRaw(obj);

		if (visual != null) {
			setup(visual, partialTick);
			visuals.put(obj, visual);
		}
	}

	@Nullable
	protected abstract Visual createRaw(T obj);

	public Plan<VisualFrameContext> framePlan() {
		return NestedPlan.of(dynamicVisuals, fastDynamicVisuals, litVisuals.plan());
	}

	public Plan<VisualTickContext> tickPlan() {
		return NestedPlan.of(tickableVisuals, fastTickableVisuals);
	}

	public void enqueueLightUpdateSections(LongSet sections) {
		litVisuals.enqueueLightUpdateSections(sections);
	}

	private void setup(Visual visual, float partialTick) {
		visual.init(partialTick);

		if (visual instanceof TickableVisual tickable) {
			if (visual instanceof SimpleTickableVisual simpleTickable) {
				fastTickableVisuals.add(simpleTickable);
			} else {
				tickableVisuals.add(tickable, tickable.planTick());
			}
		}

		if (visual instanceof DynamicVisual dynamic) {
			if (visual instanceof SimpleDynamicVisual simpleDynamic) {
				fastDynamicVisuals.add(simpleDynamic);
			} else {
				dynamicVisuals.add(dynamic, dynamic.planFrame());
			}
		}

		if (visual instanceof LitVisual lit) {
			litVisuals.addAndInitNotifier(lit);
		}
	}

	/**
	 * Is the given object currently capable of being added?
	 *
	 * @return true if the object is currently capable of being visualized.
	 */
	public abstract boolean willAccept(T obj);
}
