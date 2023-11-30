package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.MaterialShaders;
import com.jozufozu.flywheel.api.material.MaterialVertexTransformer;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class SimpleMaterial implements Material {
	protected final Runnable setup;
	protected final Runnable clear;
	protected final MaterialShaders shaders;
	protected final RenderType fallbackRenderType;
	protected final MaterialVertexTransformer vertexTransformer;

	public SimpleMaterial(Runnable setup, Runnable clear, MaterialShaders shaders, RenderType fallbackRenderType, MaterialVertexTransformer vertexTransformer) {
		this.setup = setup;
		this.clear = clear;
		this.shaders = shaders;
		this.fallbackRenderType = fallbackRenderType;
		this.vertexTransformer = vertexTransformer;
	}

	public static Builder builder() {
		return new Builder();
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
	public MaterialShaders shaders() {
		return shaders;
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
		protected Runnable setup = () -> {};
		protected Runnable clear = () -> {};
		protected MaterialShaders shaders = StandardMaterialShaders.DEFAULT;
		protected RenderType fallbackRenderType = RenderType.solid();
		protected MaterialVertexTransformer vertexTransformer = (vertexList, level) -> {};

		public Builder() {
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

		public Builder shaders(MaterialShaders shaders) {
			this.shaders = shaders;
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

		public SimpleMaterial build() {
			return new SimpleMaterial(setup, clear, shaders, fallbackRenderType, vertexTransformer);
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
