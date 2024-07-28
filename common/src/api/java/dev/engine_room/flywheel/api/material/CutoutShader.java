package dev.engine_room.flywheel.api.material;

import dev.engine_room.flywheel.api.internal.FlwApiLink;
import dev.engine_room.flywheel.api.registry.Registry;
import net.minecraft.resources.ResourceLocation;

public interface CutoutShader {
	Registry<CutoutShader> REGISTRY = FlwApiLink.INSTANCE.createRegistry();

	ResourceLocation source();
}
