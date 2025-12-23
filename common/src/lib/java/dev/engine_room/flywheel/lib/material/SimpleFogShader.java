package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.FogShader;
import net.minecraft.resources.Identifier;

public record SimpleFogShader(@Override Identifier source) implements FogShader {
}
