package dev.engine_room.flywheel.lib.visual;

import java.util.ArrayList;
import java.util.List;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.component.EntityComponent;
import net.minecraft.world.entity.Entity;

public class ComponentEntityVisual<T extends Entity> extends AbstractEntityVisual<T> implements SimpleDynamicVisual {
	protected final List<EntityComponent> components = new ArrayList<>();

	public ComponentEntityVisual(VisualizationContext ctx, T entity, float partialTick) {
		super(ctx, entity, partialTick);
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
