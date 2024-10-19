package dev.engine_room.flywheel.lib.internal;

import java.util.Deque;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.api.internal.DependencyInjection;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

public interface FlwLibLink {
	FlwLibLink INSTANCE = DependencyInjection.load(FlwLibLink.class, "dev.engine_room.flywheel.impl.FlwLibLinkImpl");

	Logger getLogger();

	PoseTransformStack getPoseTransformStackOf(PoseStack stack);

	Map<String, ModelPart> getModelPartChildren(ModelPart part);

	void compileModelPart(ModelPart part, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha);

	Deque<PoseStack.Pose> getPoseStack(PoseStack stack);

	FontSet getFontSet(Font font, ResourceLocation loc);

	boolean getFilterFishyGlyphs(Font font);

	BakedGlyphExtension getBakedGlyphExtension(BakedGlyph glyph);
}
