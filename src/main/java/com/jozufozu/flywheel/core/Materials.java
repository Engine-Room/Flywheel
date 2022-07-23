package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.core.material.SimpleMaterial;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;

public class Materials {
	private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
	public static final Material DEFAULT = ComponentRegistry.register(new SimpleMaterial(RenderStage.AFTER_BLOCK_ENTITIES, RenderType.solid(), Components.Files.DEFAULT_VERTEX, Components.Files.DEFAULT_FRAGMENT));
	public static final Material CHEST = ComponentRegistry.register(new SimpleMaterial(RenderStage.AFTER_BLOCK_ENTITIES, Sheets.chestSheet(), Components.Files.SHADED_VERTEX, Components.Files.DEFAULT_FRAGMENT));
	public static final Material SHULKER = ComponentRegistry.register(new SimpleMaterial(RenderStage.AFTER_BLOCK_ENTITIES, RenderType.entityCutoutNoCull(Sheets.SHULKER_SHEET), Components.Files.SHADED_VERTEX, Components.Files.DEFAULT_FRAGMENT));
	public static final Material BELL = ComponentRegistry.register(new SimpleMaterial(RenderStage.AFTER_BLOCK_ENTITIES, Sheets.solidBlockSheet(), Components.Files.SHADED_VERTEX, Components.Files.DEFAULT_FRAGMENT));
	public static final Material MINECART = ComponentRegistry.register(new SimpleMaterial(RenderStage.AFTER_ENTITIES, RenderType.entitySolid(MINECART_LOCATION), Components.Files.SHADED_VERTEX, Components.Files.DEFAULT_FRAGMENT));

	public static void init() {
		// noop
	}
}
