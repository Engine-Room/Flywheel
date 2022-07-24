package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.core.material.SimpleMaterial;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;

public class Materials {
	private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
	public static final Material DEFAULT = SimpleMaterial.builder()
			.stage(RenderStage.AFTER_SOLID_TERRAIN)
			.renderType(RenderType.cutout())
			.fragmentShader(Components.Files.CUTOUT_FRAGMENT)
			.register();
	public static final Material CHEST = SimpleMaterial.builder()
			.stage(RenderStage.AFTER_BLOCK_ENTITIES)
			.renderType(Sheets.chestSheet())
			.diffuseTex(Sheets.CHEST_SHEET)
			.register();
	public static final Material SHULKER = SimpleMaterial.builder()
			.stage(RenderStage.AFTER_BLOCK_ENTITIES)
			.renderType(Sheets.shulkerBoxSheet())
			.diffuseTex(Sheets.SHULKER_SHEET)
			.fragmentShader(Components.Files.CUTOUT_FRAGMENT)
			.alsoSetup(RenderSystem::disableCull)
			.alsoClear(RenderSystem::enableCull)
			.register();
	public static final Material BELL = SimpleMaterial.builder()
			.stage(RenderStage.AFTER_BLOCK_ENTITIES)
			.renderType(Sheets.solidBlockSheet())
			.register();
	public static final Material MINECART = SimpleMaterial.builder()
			.stage(RenderStage.AFTER_ENTITIES)
			.renderType(RenderType.entitySolid(MINECART_LOCATION))
			.diffuseTex(MINECART_LOCATION)
			.register();

	public static void init() {
		// noop
	}
}
