package dev.engine_room.flywheel.api.material;

import net.minecraft.resources.ResourceLocation;

public interface MaterialShaders {
	ResourceLocation vertexSource();

	ResourceLocation fragmentSource();
}
