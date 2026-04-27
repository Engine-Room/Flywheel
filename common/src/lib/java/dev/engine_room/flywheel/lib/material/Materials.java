package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;

// FIXME 1.21.11: use default blur/mipmap from AbstractTexture for all materials here, and by default
public final class Materials {
	public static final Material SOLID_BLOCK = SimpleMaterial.builder()
			.build();
	public static final Material SOLID_UNSHADED_BLOCK = SimpleMaterial.builderOf(SOLID_BLOCK)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material CUTOUT_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.build();
	public static final Material CUTOUT_UNSHADED_BLOCK = SimpleMaterial.builderOf(CUTOUT_BLOCK)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material TRANSLUCENT_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.EPSILON)
			.transparency(Transparency.ORDER_INDEPENDENT)
			.build();
	public static final Material TRANSLUCENT_UNSHADED_BLOCK = SimpleMaterial.builderOf(TRANSLUCENT_BLOCK)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material TRIPWIRE_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.transparency(Transparency.ORDER_INDEPENDENT)
			.build();
	public static final Material TRIPWIRE_UNSHADED_BLOCK = SimpleMaterial.builderOf(TRIPWIRE_BLOCK)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material GLINT = SimpleMaterial.builder()
			.texture(ItemRenderer.ENCHANTED_GLINT_ITEM)
			.shaders(StandardMaterialShaders.GLINT)
			.transparency(Transparency.GLINT)
			.writeMask(WriteMask.COLOR)
			.depthTest(DepthTest.EQUAL)
			.backfaceCulling(false)
			.build();

	// FIXME 1.21.11: missing TextureTransform.ENTITY_GLINT_TEXTURING
	public static final Material GLINT_ENTITY = SimpleMaterial.builderOf(GLINT)
			.build();

	public static final Material TRANSLUCENT_ITEM_ENTITY_BLOCK = SimpleMaterial.builder()
			.transparency(Transparency.TRANSLUCENT)
			.build();

	public static final Material TRANSLUCENT_ITEM_ENTITY_ITEM = SimpleMaterial.builder()
			.texture(TextureAtlas.LOCATION_ITEMS)
			.transparency(Transparency.TRANSLUCENT)
			.build();

	private Materials() {
	}
}
