package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.LightShader;
import net.minecraft.resources.Identifier;

public record SimpleLightShader(@Override Identifier source) implements LightShader {
}
