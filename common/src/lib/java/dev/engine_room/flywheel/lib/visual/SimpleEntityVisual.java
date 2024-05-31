package dev.engine_room.flywheel.lib.visual;

import java.util.ArrayList;
import java.util.List;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.minecraft.world.entity.Entity;

public class SimpleEntityVisual<T extends Entity> extends AbstractEntityVisual<T> implements SimpleDynamicVisual {
	protected final List<EntityComponent> components = new ArrayList<>();

	public SimpleEntityVisual(VisualizationContext ctx, T entity) {
		super(ctx, entity);
	}

	public void addComponent(EntityComponent component) {
		components.add(component);
	}

	@Override
	public void beginFrame(Context ctx) {
		for (EntityComponent component : components) {
			component.beginFrame(ctx);
		}
	}

	@Override
	protected void _delete() {
		for (EntityComponent component : components) {
			component.delete();
		}
	}
}
