package dev.engine_room.flywheel.impl.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

@Mixin(PoseStack.class)
public interface PoseStackAccessor {
	@Accessor("poses")
	List<Pose> flywheel$getPoses();
}
