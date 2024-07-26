package dev.engine_room.flywheel.api.instance;

import dev.engine_room.flywheel.api.internal.FlwApiLink;
import dev.engine_room.flywheel.api.layout.Layout;
import dev.engine_room.flywheel.api.registry.Registry;
import net.minecraft.resources.ResourceLocation;

/**
 * An InstanceType contains metadata for a specific instance that Flywheel can interface with.
 *
 * @param <I> The java representation of the instance.
 */
public interface InstanceType<I extends Instance> {
	Registry<InstanceType<?>> REGISTRY = FlwApiLink.INSTANCE.createRegistry();

	/**
	 * @param handle A handle that allows you to mark the instance as dirty or deleted.
	 * @return A new, zeroed instance of I.
	 */
	I create(InstanceHandle handle);

	Layout layout();

	InstanceWriter<I> writer();

	ResourceLocation vertexShader();

	ResourceLocation cullShader();
}
