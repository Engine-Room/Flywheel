package com.jozufozu.flywheel.core.shader;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.spec.GameStateCondition;
import com.jozufozu.flywheel.util.Pair;

public class GameStateProgram<P extends GlProgram> implements ContextAwareProgram<P> {

	private final List<Pair<GameStateCondition, P>> variants;
	private final P fallback;

	protected GameStateProgram(List<Pair<GameStateCondition, P>> variants, P fallback) {
		this.variants = variants;
		this.fallback = fallback;
	}

	@Override
	public P get() {
		for (Pair<GameStateCondition, P> variant : variants) {
            if (variant.first()
					.isMet()) return variant.second();
		}

		return fallback;
	}

	@Override
	public void delete() {
		for (Pair<GameStateCondition, P> variant : variants) {
			variant.second()
					.delete();
		}

		fallback.delete();
	}

	public static <P extends GlProgram> Builder<P> builder(P fallback) {
		return new Builder<>(fallback);
	}

	public static class Builder<P extends GlProgram> {
		private final P fallback;
		private final List<Pair<GameStateCondition, P>> variants = new ArrayList<>();

		public Builder(P fallback) {
			this.fallback = fallback;
		}

		public Builder<P> withVariant(GameStateCondition condition, P program) {
			variants.add(Pair.of(condition, program));
			return this;
		}

		public ContextAwareProgram<P> build() {
			return new GameStateProgram<>(ImmutableList.copyOf(variants), fallback);
		}
	}
}
