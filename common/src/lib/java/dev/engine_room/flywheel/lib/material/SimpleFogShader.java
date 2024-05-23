package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.FogShader;
import net.minecraft.resources.ResourceLocation;

public record SimpleFogShader(@Override ResourceLocation source) implements FogShader {
}
