package dev.engine_room.flywheel.api.material;

import dev.engine_room.flywheel.api.internal.FlwApiLink;
import dev.engine_room.flywheel.api.registry.Registry;
import net.minecraft.resources.ResourceLocation;

public interface MaterialShaders {
	static Registry<MaterialShaders> REGISTRY = FlwApiLink.INSTANCE.createRegistry();

	ResourceLocation vertexShader();

	ResourceLocation fragmentShader();
}
