package dev.engine_room.flywheel.impl.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;

@Mixin(ModelPart.class)
public interface ModelPartAccessor {
	@Accessor("children")
	Map<String, ModelPart> flywheel$children();

	@Invoker("compile")
	void flywheel$compile(PoseStack.Pose pose, VertexConsumer buffer, int packedLight, int packedOverlay, int color);
}
