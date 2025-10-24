package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import net.minecraft.client.renderer.entity.ItemRenderer;

public final class Materials {
	public static final Material SOLID_BLOCK = SimpleMaterial.builder()
			.build();
	public static final Material SOLID_UNSHADED_BLOCK = SimpleMaterial.builder()
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material CUTOUT_MIPPED_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.build();
	public static final Material CUTOUT_MIPPED_UNSHADED_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material CUTOUT_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.mipmap(false)
			.build();
	public static final Material CUTOUT_UNSHADED_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.mipmap(false)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material TRANSLUCENT_BLOCK = SimpleMaterial.builder()
			.transparency(Transparency.ORDER_INDEPENDENT)
			.build();
	public static final Material TRANSLUCENT_UNSHADED_BLOCK = SimpleMaterial.builder()
			.transparency(Transparency.ORDER_INDEPENDENT)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material TRIPWIRE_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.transparency(Transparency.ORDER_INDEPENDENT)
			.build();
	public static final Material TRIPWIRE_UNSHADED_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.transparency(Transparency.ORDER_INDEPENDENT)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material GLINT = SimpleMaterial.builder()
			.texture(ItemRenderer.ENCHANTED_GLINT_ITEM)
			.shaders(StandardMaterialShaders.GLINT)
			.transparency(Transparency.GLINT)
			.writeMask(WriteMask.COLOR)
			.depthTest(DepthTest.EQUAL)
			.backfaceCulling(false)
			.blur(true)
			.mipmap(false)
			.build();

	public static final Material GLINT_ENTITY = SimpleMaterial.builderOf(GLINT)
			.texture(ItemRenderer.ENCHANTED_GLINT_ENTITY)
			.build();

	public static final Material TRANSLUCENT_ENTITY = SimpleMaterial.builder()
			.transparency(Transparency.TRANSLUCENT)
			.cutout(CutoutShaders.ONE_TENTH)
			.mipmap(false)
			.build();

	private Materials() {
	}
}
