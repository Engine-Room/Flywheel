package dev.engine_room.flywheel.lib.visual.text;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

public record SimpleTextLayer(GlyphMeshStyle style, GlyphMaterial material, GlyphColor color, int bias, float offsetX,
							  float offsetY, float effectOffsetX, float effectOffsetY) implements TextLayer {
	public static class Builder {
		@Nullable
		private GlyphMeshStyle style;
		@Nullable
		private GlyphMaterial material;
		@Nullable
		private GlyphColor color;

		private int bias;
		private float offsetX = 0;
		private float offsetY = 0;
		private float effectOffsetX = 1;
		private float effectOffsetY = 1;

		public Builder style(GlyphMeshStyle style) {
			this.style = style;
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

		public Builder bias(int bias) {
			this.bias = bias;
			return this;
		}

		public Builder offsetX(float offsetX) {
			this.offsetX = offsetX;
			return this;
		}

		public Builder offsetY(float offsetY) {
			this.offsetY = offsetY;
			return this;
		}

		public Builder effectOffsetX(float effectOffsetX) {
			this.effectOffsetX = effectOffsetX;
			return this;
		}

		public Builder effectOffsetY(float effectOffsetY) {
			this.effectOffsetY = effectOffsetY;
			return this;
		}

		public SimpleTextLayer build() {
			Objects.requireNonNull(style);
			Objects.requireNonNull(material);
			Objects.requireNonNull(color);

			return new SimpleTextLayer(style, material, color, bias, offsetX, offsetY, effectOffsetX, effectOffsetY);
		}
	}
}
