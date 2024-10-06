package dev.engine_room.flywheel.lib.visual.text;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public record SimpleTextLayer(GlyphPattern pattern, GlyphMaterial material, GlyphColor color, Vector2fc offset, int bias) implements TextLayer {
	public static class Builder {
		private static final Vector2fc NO_OFFSET = new Vector2f();

		@Nullable
		private GlyphPattern pattern;
		@Nullable
		private GlyphMaterial material;
		@Nullable
		private GlyphColor color;
		private Vector2fc offset = NO_OFFSET;
		private int bias = 0;

		public Builder pattern(GlyphPattern pattern) {
			this.pattern = pattern;
			return this;
		}

		public Builder material(GlyphMaterial material) {
			this.material = material;
			return this;
		}

		public Builder color(GlyphColor color) {
			this.color = color;
			return this;
		}

		public Builder offset(Vector2fc offset) {
			this.offset = offset;
			return this;
		}

		public Builder offset(float offsetX, float offsetY) {
			offset = new Vector2f(offsetX, offsetY);
			return this;
		}

		public Builder bias(int bias) {
			this.bias = bias;
			return this;
		}

		public SimpleTextLayer build() {
			Objects.requireNonNull(pattern);
			Objects.requireNonNull(material);
			Objects.requireNonNull(color);

			return new SimpleTextLayer(pattern, material, color, offset, bias);
		}
	}
}
