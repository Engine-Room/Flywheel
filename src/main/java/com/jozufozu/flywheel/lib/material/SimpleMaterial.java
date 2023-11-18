package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.MaterialVertexTransformer;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SimpleMaterial implements Material {
	protected final ResourceLocation vertexShader;
	protected final ResourceLocation fragmentShader;
	protected final Runnable setup;
	protected final Runnable clear;
	protected final RenderType fallbackRenderType;
	protected final MaterialVertexTransformer vertexTransformer;

	public SimpleMaterial(ResourceLocation vertexShader, ResourceLocation fragmentShader, Runnable setup, Runnable clear, RenderType fallbackRenderType, MaterialVertexTransformer vertexTransformer) {
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		this.setup = setup;
		this.clear = clear;
		this.fallbackRenderType = fallbackRenderType;
		this.vertexTransformer = vertexTransformer;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public ResourceLocation vertexShader() {
		return vertexShader;
	}

	@Override
	public ResourceLocation fragmentShader() {
		return fragmentShader;
	}

	@Override
	public void setup() {
		setup.run();
	}

	@Override
	public void clear() {
		clear.run();
	}

	@Override
	public RenderType getFallbackRenderType() {
		return fallbackRenderType;
	}

	@Override
	public MaterialVertexTransformer getVertexTransformer() {
		return vertexTransformer;
	}

	public static class Builder {
		protected ResourceLocation vertexShader = Materials.Files.DEFAULT_VERTEX;
		protected ResourceLocation fragmentShader = Materials.Files.DEFAULT_FRAGMENT;
		protected Runnable setup = () -> {};
		protected Runnable clear = () -> {};
		protected RenderType fallbackRenderType = RenderType.solid();
		protected MaterialVertexTransformer vertexTransformer = (vertexList, level) -> {};

		public Builder() {
		}

		public Builder vertexShader(ResourceLocation vertexShader) {
			this.vertexShader = vertexShader;
			return this;
		}

		public Builder fragmentShader(ResourceLocation fragmentShader) {
			this.fragmentShader = fragmentShader;
			return this;
		}

		public Builder addSetup(Runnable setup) {
			this.setup = chain(this.setup, setup);
			return this;
		}

		public Builder addClear(Runnable clear) {
			this.clear = chain(this.clear, clear);
			return this;
		}

		public Builder addShard(GlStateShard shard) {
			addSetup(shard.getSetup());
			addClear(shard.getClear());
			return this;
		}

		public Builder fallbackRenderType(RenderType type) {
			this.fallbackRenderType = type;
			return this;
		}

		public Builder vertexTransformer(MaterialVertexTransformer vertexTransformer) {
			this.vertexTransformer = vertexTransformer;
			return this;
		}

		public SimpleMaterial register() {
			return Material.REGISTRY.registerAndGet(new SimpleMaterial(vertexShader, fragmentShader, setup, clear, fallbackRenderType, vertexTransformer));
		}

		private static Runnable chain(Runnable runnable1, Runnable runnable2) {
			return () -> {
				runnable1.run();
				runnable2.run();
			};
		}
	}

	public static class GlStateShard {
		protected final Runnable setup;
		protected final Runnable clear;

		public GlStateShard(Runnable setup, Runnable clear) {
			this.setup = setup;
			this.clear = clear;
		}

		public static GlStateShard fromVanilla(RenderStateShard vanillaShard) {
			return new GlStateShard(vanillaShard::setupRenderState, vanillaShard::clearRenderState);
		}

		public Runnable getSetup() {
			return setup;
		}

		public Runnable getClear() {
			return clear;
		}
	}
}
