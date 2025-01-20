package dev.engine_room.vanillin.mixin.text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.font.FontTexture$Node")
public interface FontTexture$NodeAccessor {
	@Accessor("x")
	int flywheel$getX();

	@Accessor("y")
	int flywheel$getY();
}
