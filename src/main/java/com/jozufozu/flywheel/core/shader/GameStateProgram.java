package com.jozufozu.flywheel.core.shader;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.spec.IGameStateCondition;
import com.jozufozu.flywheel.util.Pair;

public class GameStateProgram<P extends GlProgram> implements IMultiProgram<P> {

	private final List<Pair<IGameStateCondition, P>> variants;
	private final P fallback;

	protected GameStateProgram(List<Pair<IGameStateCondition, P>> variants, P fallback) {
		this.variants = variants;
		this.fallback = fallback;
	}

	@Override
	public P get() {
		for (Pair<IGameStateCondition, P> variant : variants) {
			if (variant.getFirst()
					.isMet()) return variant.getSecond();
		}

		return fallback;
	}

	@Override
	public void delete() {
		for (Pair<IGameStateCondition, P> variant : variants) {
			variant.getSecond()
					.delete();
		}

		fallback.delete();
	}

	public static <P extends GlProgram> Builder<P> builder(P fallback) {
		return new Builder<>(fallback);
	}

	public static class Builder<P extends GlProgram> {
		private final P fallback;
		private final List<Pair<IGameStateCondition, P>> variants = new ArrayList<>();

		public Builder(P fallback) {
			this.fallback = fallback;
		}

		public Builder<P> withVariant(IGameStateCondition condition, P program) {
			variants.add(Pair.of(condition, program));
			return this;
		}

		public IMultiProgram<P> build() {
			return new GameStateProgram<>(ImmutableList.copyOf(variants), fallback);
		}
	}
}
