package com.jozufozu.flywheel.lib.material;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.MaterialVertexTransformer;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.jozufozu.flywheel.lib.material.SimpleMaterial.GlStateShard;
import com.jozufozu.flywheel.lib.math.DiffuseLightCalculator;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class Materials {
	public static final MaterialVertexTransformer SHADING_TRANSFORMER = (vertexList, level) -> {
		if (ShadersModHandler.isShaderPackInUse()) {
			return;
		}

		DiffuseLightCalculator diffuseCalc = DiffuseLightCalculator.forLevel(level);
		for (int i = 0; i < vertexList.vertexCount(); i++) {
			float diffuse = diffuseCalc.getDiffuse(vertexList.normalX(i), vertexList.normalY(i), vertexList.normalZ(i), true);
			vertexList.r(i, vertexList.r(i) * diffuse);
			vertexList.g(i, vertexList.g(i) * diffuse);
			vertexList.b(i, vertexList.b(i) * diffuse);
		}
	};

	private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");

	public static final Material CHUNK_SOLID_SHADED = SimpleMaterial.builder()
			.vertexShader(Files.SHADED_VERTEX)
			.fragmentShader(Files.DEFAULT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.fallbackRenderType(RenderType.solid())
			.vertexTransformer(SHADING_TRANSFORMER)
			.register();
	public static final Material CHUNK_SOLID_UNSHADED = SimpleMaterial.builder()
			.vertexShader(Files.DEFAULT_VERTEX)
			.fragmentShader(Files.DEFAULT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.fallbackRenderType(RenderType.solid())
			.register();

	public static final Material CHUNK_CUTOUT_MIPPED_SHADED = SimpleMaterial.builder()
			.vertexShader(Files.SHADED_VERTEX)
			.fragmentShader(Files.CUTOUT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.fallbackRenderType(RenderType.cutoutMipped())
			.vertexTransformer(SHADING_TRANSFORMER)
			.register();
	public static final Material CHUNK_CUTOUT_MIPPED_UNSHADED = SimpleMaterial.builder()
			.vertexShader(Files.DEFAULT_VERTEX)
			.fragmentShader(Files.CUTOUT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.fallbackRenderType(RenderType.cutoutMipped())
			.register();

	public static final Material CHUNK_CUTOUT_SHADED = SimpleMaterial.builder()
			.vertexShader(Files.SHADED_VERTEX)
			.fragmentShader(Files.CUTOUT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, false))
			.fallbackRenderType(RenderType.cutout())
			.vertexTransformer(SHADING_TRANSFORMER)
			.register();
	public static final Material CHUNK_CUTOUT_UNSHADED = SimpleMaterial.builder()
			.vertexShader(Files.DEFAULT_VERTEX)
			.fragmentShader(Files.CUTOUT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, false))
			.fallbackRenderType(RenderType.cutout())
			.register();

	public static final Material CHUNK_TRANSLUCENT_SHADED = SimpleMaterial.builder()
			.vertexShader(Files.SHADED_VERTEX)
			.fragmentShader(Files.DEFAULT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.addShard(Shards.TRANSLUCENT_TRANSPARENCY)
			.fallbackRenderType(RenderType.translucent())
			.vertexTransformer(SHADING_TRANSFORMER)
			.register();
	public static final Material CHUNK_TRANSLUCENT_UNSHADED = SimpleMaterial.builder()
			.vertexShader(Files.DEFAULT_VERTEX)
			.fragmentShader(Files.DEFAULT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.addShard(Shards.TRANSLUCENT_TRANSPARENCY)
			.fallbackRenderType(RenderType.translucent())
			.register();

	public static final Material CHUNK_TRIPWIRE_SHADED = SimpleMaterial.builder()
			.vertexShader(Files.SHADED_VERTEX)
			.fragmentShader(Files.CUTOUT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.addShard(Shards.TRANSLUCENT_TRANSPARENCY)
			.fallbackRenderType(RenderType.tripwire())
			.vertexTransformer(SHADING_TRANSFORMER)
			.register();
	public static final Material CHUNK_TRIPWIRE_UNSHADED = SimpleMaterial.builder()
			.vertexShader(Files.DEFAULT_VERTEX)
			.fragmentShader(Files.CUTOUT_FRAGMENT)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, true))
			.addShard(Shards.TRANSLUCENT_TRANSPARENCY)
			.fallbackRenderType(RenderType.tripwire())
			.register();

	public static final Material CHEST = SimpleMaterial.builder()
			.vertexShader(Files.SHADED_VERTEX)
			.addShard(Shards.diffuseTex(Sheets.CHEST_SHEET, false, false))
			.fallbackRenderType(Sheets.chestSheet())
			.register();
	public static final Material SHULKER = SimpleMaterial.builder()
			.vertexShader(Files.SHADED_VERTEX)
			.fragmentShader(Files.CUTOUT_FRAGMENT)
			.addShard(Shards.diffuseTex(Sheets.SHULKER_SHEET, false, false))
			.addShard(Shards.DISABLE_CULL)
			.fallbackRenderType(Sheets.shulkerBoxSheet())
			.register();
	public static final Material BELL = SimpleMaterial.builder()
			.vertexShader(Files.SHADED_VERTEX)
			.addShard(Shards.diffuseTex(InventoryMenu.BLOCK_ATLAS, false, false))
			.fallbackRenderType(Sheets.solidBlockSheet())
			.register();
	public static final Material MINECART = SimpleMaterial.builder()
			.vertexShader(Files.SHADED_VERTEX)
			.addShard(Shards.diffuseTex(MINECART_LOCATION, false, false))
			.fallbackRenderType(RenderType.entitySolid(MINECART_LOCATION))
			.register();

	private Materials() {
	}

	@ApiStatus.Internal
	public static void init() {
	}

	public static final class Shards {
		public static final GlStateShard DISABLE_CULL = new GlStateShard(
				() -> {
					RenderSystem.disableCull();
				},
				() -> {
					RenderSystem.enableCull();
				}
		);

		public static final GlStateShard TRANSLUCENT_TRANSPARENCY = new GlStateShard(
				() -> {
					RenderSystem.enableBlend();
					RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				},
				() -> {
					RenderSystem.disableBlend();
					RenderSystem.defaultBlendFunc();
				}
		);

		public static GlStateShard diffuseTex(ResourceLocation loc, boolean blur, boolean mipmap) {
			return new GlStateShard(
					() -> {
						GlTextureUnit.T0.makeActive();
						AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(loc);
						texture.setFilter(blur, mipmap);
						RenderSystem.setShaderTexture(0, texture.getId());
					},
					() -> {
						GlTextureUnit.T0.makeActive();
						RenderSystem.setShaderTexture(0, 0);
					}
			);
		}
	}

	public static final class Files {
		public static final ResourceLocation DEFAULT_VERTEX = Names.DEFAULT.withSuffix(".vert");
		public static final ResourceLocation SHADED_VERTEX = Names.SHADED.withSuffix(".vert");
		public static final ResourceLocation DEFAULT_FRAGMENT = Names.DEFAULT.withSuffix(".frag");
		public static final ResourceLocation CUTOUT_FRAGMENT = Names.CUTOUT.withSuffix(".frag");
	}

	public static final class Names {
		public static final ResourceLocation DEFAULT = Flywheel.rl("material/default");
		public static final ResourceLocation CUTOUT = Flywheel.rl("material/cutout");
		public static final ResourceLocation SHADED = Flywheel.rl("material/shaded");
	}
}
