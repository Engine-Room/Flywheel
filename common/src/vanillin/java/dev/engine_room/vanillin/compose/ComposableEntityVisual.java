package dev.engine_room.vanillin.compose;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.EntityVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visual.Visual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.minecraft.world.entity.Entity;

public class ComposableEntityVisual<T extends Entity> extends AbstractVisual implements EntityVisual<T>, SimpleTickableVisual, SimpleDynamicVisual {
	private final T entity;
	private final Controller<T> controller;
	// Parallel array to Controller.elements mapping element configurations to instantiated visuals.
	private final @Nullable Visual[] visuals;

	// TODO: compute the required interfaces this visual needs to implement to cover the interfaces implemented
	//  by its elements. Proxy seems like it could be a good candidate, but regardless we need to know ahead of
	//  time which interfaces to implement. I have a feeling that configured elements will need to know what class
	//  of visual they create.
	public ComposableEntityVisual(VisualizationContext ctx, T entity, float partialTick, Controller<T> controller) {
		super(ctx, entity.level(), partialTick);
		this.entity = entity;
		this.controller = controller;
		this.visuals = new Visual[controller.elements.length];

		updateElements(partialTick);
	}

	@Override
	public void tick(TickableVisual.Context context) {
		updateElements(0.0f);

		for (var visual : visuals) {
			if (visual instanceof SimpleTickableVisual tickable) {
				tickable.tick(context);
			}
		}
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		updateElements(ctx.partialTick());

		for (var visual : visuals) {
			if (visual instanceof SimpleDynamicVisual dynamic) {
				dynamic.beginFrame(ctx);
			}
		}
	}

	private void updateElements(float partialTick) {
		if (!controller.predicate.shouldVisualize(visualizationContext, entity)) {
			for (var i = 0; i < visuals.length; i++) {
				if (visuals[i] != null) {
					visuals[i].delete();
					visuals[i] = null;
				}
			}

			return;
		}

		// Create/delete visual elements as necessary.
		for (var i = 0; i < controller.elements.length; i++) {
			var element = controller.elements[i];

			var shouldExist = element.shouldVisualize(visualizationContext, entity);
			var exists = visuals[i] != null;
			if (shouldExist && !exists) {
				visuals[i] = element.create(visualizationContext, entity, partialTick);
			} else if (!shouldExist && exists) {
				visuals[i].delete();
				visuals[i] = null;
			}
		}
	}

	@Override
	protected void _delete() {
		for (var visual : visuals) {
			if (visual != null) {
				visual.delete();
			}
		}
	}

	/**
	 * Shared state between all visuals of the same type. Wrap it in a class so that the actual Visual class can be smaller.
	 */
	public static class Controller<T> {
		private final ConfiguredElement<? super T>[] elements;
		private final VisualizationPredicate<T> predicate;

		public Controller(ConfiguredElement<? super T>[] elements, VisualizationPredicate<T> predicate) {
			this.elements = elements;
			this.predicate = predicate;
		}
	}
}
