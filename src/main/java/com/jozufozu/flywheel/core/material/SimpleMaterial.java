package com.jozufozu.flywheel.core.material;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class SimpleMaterial implements Material {
	protected final RenderStage stage;
	protected final RenderType type;
	protected final FileResolution vertexShader;
	protected final FileResolution fragmentShader;
	protected final ResourceLocation diffuseTex;
	@Nullable
	protected final Runnable setup;
	@Nullable
	protected final Runnable clear;

	public SimpleMaterial(RenderStage stage, RenderType type, FileResolution vertexShader, FileResolution fragmentShader, ResourceLocation diffuseTex, @Nullable Runnable setup, @Nullable Runnable clear) {
		this.stage = stage;
		this.type = type;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		this.diffuseTex = diffuseTex;
		this.setup = setup;
		this.clear = clear;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public RenderStage getRenderStage() {
		return stage;
	}

	@Override
	public RenderType getBatchingRenderType() {
		return type;
	}

	@Override
	public FileResolution getVertexShader() {
		return vertexShader;
	}

	@Override
	public FileResolution getFragmentShader() {
		return fragmentShader;
	}

	@Override
	public void setup() {

		GlTextureUnit.T0.makeActive();
		RenderSystem.setShaderTexture(0, diffuseTex);
		Minecraft.getInstance().textureManager.bindForSetup(diffuseTex);

		if (setup != null) {
			setup.run();
		}
	}

	@Override
	public void clear() {
		GlTextureUnit.T0.makeActive();
		RenderSystem.setShaderTexture(0, 0);

		if (clear != null) {
			clear.run();
		}
	}

	public static class Builder {
		protected RenderStage stage = RenderStage.AFTER_SOLID_TERRAIN;
		protected RenderType type = RenderType.solid();
		protected FileResolution vertexShader = Components.Files.SHADED_VERTEX;
		protected FileResolution fragmentShader = Components.Files.DEFAULT_FRAGMENT;
		protected ResourceLocation diffuseTex = InventoryMenu.BLOCK_ATLAS;
		protected Runnable setup = null;
		protected Runnable clear = null;

		public Builder() {
		}

		public Builder stage(RenderStage stage) {
			this.stage = stage;
			return this;
		}

		public Builder renderType(RenderType type) {
			this.type = type;
			return this;
		}

		public Builder vertexShader(FileResolution vertexShader) {
			this.vertexShader = vertexShader;
			return this;
		}

		public Builder fragmentShader(FileResolution fragmentShader) {
			this.fragmentShader = fragmentShader;
			return this;
		}

		public Builder shaded() {
			this.vertexShader = Components.Files.SHADED_VERTEX;
			return this;
		}

		public Builder unShaded() {
			this.vertexShader = Components.Files.DEFAULT_VERTEX;
			return this;
		}

		public Builder diffuseTex(ResourceLocation diffuseTex) {
			this.diffuseTex = diffuseTex;
			return this;
		}

		public Builder alsoSetup(Runnable runnable) {
			this.setup = runnable;
			return this;
		}

		public Builder alsoClear(Runnable clear) {
			this.clear = clear;
			return this;
		}

		public SimpleMaterial register() {
			return ComponentRegistry.register(new SimpleMaterial(stage, type, vertexShader, fragmentShader, diffuseTex, setup, clear));
		}
	}
}
