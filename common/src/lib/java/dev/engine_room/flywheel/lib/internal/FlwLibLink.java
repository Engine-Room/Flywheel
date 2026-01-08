package dev.engine_room.flywheel.lib.internal;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.api.internal.DependencyInjection;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public interface FlwLibLink {
	FlwLibLink INSTANCE = DependencyInjection.load(FlwLibLink.class, "dev.engine_room.flywheel.impl.FlwLibLinkImpl");

	Logger getLogger();

	PoseTransformStack getPoseTransformStackOf(PoseStack stack);

	Map<String, ModelPart> getModelPartChildren(ModelPart part);

	void compileModelPart(ModelPart part, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, int color);

	List<Pose> getPoses(PoseStack stack);

	<T extends Entity> boolean affectedByCulling(T entity);

	<T extends Entity> AABB getBoundingBoxForCulling(T entity);

	boolean isIrisLoaded();

	boolean isOptifineInstalled();

	boolean isShaderPackInUse();

	boolean isRenderingShadowPass();
}
