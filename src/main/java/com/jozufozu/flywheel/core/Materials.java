package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.api.material.Material;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;

public class Materials {
	private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
	public static final Material DEFAULT = ComponentRegistry.register(new Material(RenderType.solid(), Components.Files.DEFAULT_VERTEX, Components.Files.DEFAULT_FRAGMENT));
	public static final Material CHEST = ComponentRegistry.register(new Material(Sheets.chestSheet(), Components.Files.SHADED_VERTEX, Components.Files.DEFAULT_FRAGMENT));
	public static final Material SHULKER = ComponentRegistry.register(new Material(RenderType.entityCutoutNoCull(Sheets.SHULKER_SHEET), Components.Files.SHADED_VERTEX, Components.Files.DEFAULT_FRAGMENT));
	public static final Material MINECART = ComponentRegistry.register(new Material(RenderType.entitySolid(MINECART_LOCATION), Components.Files.SHADED_VERTEX, Components.Files.DEFAULT_FRAGMENT));

	public static void init() {
		// noop
	}
}
