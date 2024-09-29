package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import net.minecraft.client.renderer.entity.ItemRenderer;

public final class Materials {
	public static final Material SOLID_BLOCK = SimpleMaterial.builder()
			.build();
	public static final Material SOLID_UNSHADED_BLOCK = SimpleMaterial.builder()
			.diffuse(false)
			.build();

	public static final Material CUTOUT_MIPPED_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.build();
	public static final Material CUTOUT_MIPPED_UNSHADED_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.diffuse(false)
			.build();

	public static final Material CUTOUT_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.mipmap(false)
			.build();
	public static final Material CUTOUT_UNSHADED_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.mipmap(false)
			.diffuse(false)
			.build();

	public static final Material TRANSLUCENT_BLOCK = SimpleMaterial.builder()
			.transparency(Transparency.TRANSLUCENT)
			.build();
	public static final Material TRANSLUCENT_UNSHADED_BLOCK = SimpleMaterial.builder()
			.transparency(Transparency.TRANSLUCENT)
			.diffuse(false)
			.build();

	public static final Material TRIPWIRE_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.transparency(Transparency.TRANSLUCENT)
			.build();
	public static final Material TRIPWIRE_UNSHADED_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.transparency(Transparency.TRANSLUCENT)
			.diffuse(false)
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

	private Materials() {
	}
}
