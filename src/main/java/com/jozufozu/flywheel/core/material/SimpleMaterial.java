package com.jozufozu.flywheel.core.material;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.source.FileResolution;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class SimpleMaterial implements Material {
	protected final RenderStage stage;
	protected final FileResolution vertexShader;
	protected final FileResolution fragmentShader;
	protected final Runnable setup;
	protected final Runnable clear;
	protected final RenderType batchingRenderType;
	protected final VertexTransformer vertexTransformer;

	public SimpleMaterial(RenderStage stage, FileResolution vertexShader, FileResolution fragmentShader, Runnable setup, Runnable clear, RenderType batchingRenderType, VertexTransformer vertexTransformer) {
		this.stage = stage;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		this.setup = setup;
		this.clear = clear;
		this.batchingRenderType = batchingRenderType;
		this.vertexTransformer = vertexTransformer;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public RenderStage getRenderStage() {
		return stage;
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
		setup.run();
	}

	@Override
	public void clear() {
		clear.run();
	}

	@Override
	public RenderType getBatchingRenderType() {
		return batchingRenderType;
	}

	@Override
	public VertexTransformer getVertexTransformer() {
		return vertexTransformer;
	}

	public static class Builder {
		protected RenderStage stage = RenderStage.AFTER_SOLID_TERRAIN;
		protected FileResolution vertexShader = Components.Files.DEFAULT_VERTEX;
		protected FileResolution fragmentShader = Components.Files.DEFAULT_FRAGMENT;
		protected Runnable setup = () -> {};
		protected Runnable clear = () -> {};
		protected RenderType batchingRenderType = RenderType.solid();
		protected VertexTransformer vertexTransformer = (vertexList, level) -> {};

		public Builder() {
		}

		public Builder stage(RenderStage stage) {
			this.stage = stage;
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

		public Builder batchingRenderType(RenderType type) {
			this.batchingRenderType = type;
			return this;
		}

		public Builder vertexTransformer(VertexTransformer vertexTransformer) {
			this.vertexTransformer = vertexTransformer;
			return this;
		}

		public SimpleMaterial register() {
			return ComponentRegistry.register(new SimpleMaterial(stage, vertexShader, fragmentShader, setup, clear, batchingRenderType, vertexTransformer));
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
			return new GlStateShard(() -> vanillaShard.setupRenderState(), () -> vanillaShard.clearRenderState());
		}

		public Runnable getSetup() {
			return setup;
		}

		public Runnable getClear() {
			return clear;
		}
	}
}
