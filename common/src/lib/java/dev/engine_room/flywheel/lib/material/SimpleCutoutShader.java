package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.CutoutShader;
import net.minecraft.resources.Identifier;

public record SimpleCutoutShader(@Override Identifier source) implements CutoutShader {
}
