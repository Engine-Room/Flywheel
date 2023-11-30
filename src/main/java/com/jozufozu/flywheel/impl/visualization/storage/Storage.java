package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.PlannedVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.Visual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.lib.task.ForEachPlan;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public abstract class Storage<T> {
	protected final Engine engine;
	protected final List<TickableVisual> tickableVisuals = new ArrayList<>();
	protected final List<DynamicVisual> dynamicVisuals = new ArrayList<>();
	protected final List<PlannedVisual> plannedVisuals = new ArrayList<>();
	protected final VisualUpdatePlan<VisualFrameContext> framePlan = new VisualUpdatePlan<>(() -> plannedVisuals.stream()
			.map(PlannedVisual::planFrame)
			.toList());
	protected final VisualUpdatePlan<VisualTickContext> tickPlan = new VisualUpdatePlan<>(() -> plannedVisuals.stream()
			.map(PlannedVisual::planTick)
			.toList());

	private final Map<T, Visual> visuals = new Reference2ObjectOpenHashMap<>();

	public Storage(Engine engine) {
		this.engine = engine;
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

		tickableVisuals.remove(visual);
		dynamicVisuals.remove(visual);
		if (plannedVisuals.remove(visual)) {
			framePlan.triggerReInitialize();
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
		dynamicVisuals.clear();
		plannedVisuals.clear();
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
		plannedVisuals.clear();
		framePlan.triggerReInitialize();
		tickPlan.triggerReInitialize();
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

	public Plan<VisualFrameContext> getFramePlan() {
		return framePlan.and(ForEachPlan.of(() -> dynamicVisuals, DynamicVisual::beginFrame));
	}

	public Plan<VisualTickContext> getTickPlan() {
		return tickPlan.and(ForEachPlan.of(() -> tickableVisuals, TickableVisual::tick));
	}

	private void setup(Visual visual, float partialTick) {
		visual.init(partialTick);

		if (visual instanceof TickableVisual tickable) {
			tickableVisuals.add(tickable);
		}

		if (visual instanceof DynamicVisual dynamic) {
			dynamicVisuals.add(dynamic);
		}

		if (visual instanceof PlannedVisual planned) {
			plannedVisuals.add(planned);
			framePlan.add(planned.planFrame());
			tickPlan.add(planned.planTick());
		}
	}

	/**
	 * Is the given object currently capable of being added?
	 *
	 * @return true if the object is currently capable of being visualized.
	 */
	public abstract boolean willAccept(T obj);
}
