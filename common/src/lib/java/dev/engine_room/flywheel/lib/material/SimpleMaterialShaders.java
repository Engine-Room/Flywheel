package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.MaterialShaders;
import net.minecraft.resources.ResourceLocation;

public record SimpleMaterialShaders(ResourceLocation vertexShader, ResourceLocation fragmentShader) implements MaterialShaders {
}
