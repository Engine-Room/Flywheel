package dev.engine_room.flywheel.backend.engine;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.SectionPos;

// Massive kudos to RogueLogix for figuring out this LUT scheme.
// First layer is Y, then X, then Z.
public final class LightLut {
	public final Layer<Layer<Layer<IntLayer>>> indices = new Layer<>();

	public void add(int scene, long position, int index) {
		final var x = SectionPos.x(position);
		final var y = SectionPos.y(position);
		final var z = SectionPos.z(position);

		indices.computeIfAbsent(scene, Layer::new)
				.computeIfAbsent(y, Layer::new)
				.computeIfAbsent(x, IntLayer::new)
				.set(z, index + 1);
	}

	public void prune() {
		// Maybe this could be done better incrementally?
		indices.prune((scene) -> scene.prune((middle) -> middle.prune(IntLayer::prune)));
	}

	public void remove(int scene, long section) {
		final var x = SectionPos.x(section);
		final var y = SectionPos.y(section);
		final var z = SectionPos.z(section);

		var first = indices.get(scene);

		if (first == null) {
			return;
		}

		var second = first.get(y);

		if (second == null) {
			return;
		}

		var third = second.get(x);

		if (third == null) {
			return;
		}

		third.clear(z);
	}

	public IntArrayList flatten() {
		final var out = new IntArrayList();
		this.indices.fillLut(out, (sceneIndices, lut1) -> sceneIndices.fillLut(lut1,
				(yIndices, lut2) -> yIndices.fillLut(lut2, IntLayer::fillLut)
		));
		return out;
	}

	@FunctionalInterface
	public interface Prune<T> {
		boolean prune(T t);
	}

	public static final class Layer<T> {
		private boolean hasBase = false;
		private int base = 0;
		private Object[] nextLayer = new Object[0];

		public void fillLut(IntArrayList lut, BiConsumer<T, IntArrayList> inner) {
			lut.add(base);
			lut.add(nextLayer.length);

			int innerIndexBase = lut.size();

			// Reserve space for the inner indices...
			lut.size(innerIndexBase + nextLayer.length);

			for (int i = 0; i < nextLayer.length; i++) {
				final var innerIndices = (T) nextLayer[i];
				if (innerIndices == null) {
					continue;
				}

				int layerPosition = lut.size();

				// ...so we can write in their actual positions later.
				lut.set(innerIndexBase + i, layerPosition);

				// Append the next layer to the lut.
				inner.accept(innerIndices, lut);
			}
		}

		public int base() {
			return base;
		}

		public int size() {
			return nextLayer.length;
		}

		@Nullable
		public T getRaw(int i) {
			if (i < 0) {
				return null;
			}

			if (i >= nextLayer.length) {
				return null;
			}

			return (T) nextLayer[i];
		}

		@Nullable
		public T get(int i) {
			if (!hasBase) {
				return null;
			}

			return getRaw(i - base);
		}

		public T computeIfAbsent(int i, Supplier<T> ifAbsent) {
			if (!hasBase) {
				// We don't want to default to base 0, so we'll use the first value we get.
				base = i;
				hasBase = true;
			}

			if (i < base) {
				rebase(i);
			}

			final var offset = i - base;

			if (offset >= nextLayer.length) {
				resize(offset + 1);
			}

			var out = nextLayer[offset];

			if (out == null) {
				out = ifAbsent.get();
				nextLayer[offset] = out;
			}
			return (T) out;
		}

		/**
		 * @return {@code true} if the layer is now empty.
		 */
		public boolean prune(Prune<T> inner) {
			if (!hasBase) {
				return true;
			}

			// Prune the next layer before checking for leading/trailing zeros.
			for (var i = 0; i < nextLayer.length; i++) {
				var o = nextLayer[i];
				if (o != null && inner.prune((T) o)) {
					nextLayer[i] = null;
				}
			}

			var leadingZeros = getLeadingZeros();

			if (leadingZeros == nextLayer.length) {
				return true;
			}

			var trailingZeros = getTrailingZeros();

			if (leadingZeros == 0 && trailingZeros == 0) {
				return false;
			}

			final var newIndices = new Object[nextLayer.length - leadingZeros - trailingZeros];

			System.arraycopy(nextLayer, leadingZeros, newIndices, 0, newIndices.length);
			nextLayer = newIndices;
			base += leadingZeros;

			return false;
		}

		private int getLeadingZeros() {
			int out = 0;

			for (Object index : nextLayer) {
				if (index == null) {
					out++;
				} else {
					break;
				}
			}
			return out;
		}

		private int getTrailingZeros() {
			int out = 0;

			for (int i = nextLayer.length - 1; i >= 0; i--) {
				if (nextLayer[i] == null) {
					out++;
				} else {
					break;
				}
			}
			return out;
		}

		private void resize(int length) {
			final var newIndices = new Object[length];
			System.arraycopy(nextLayer, 0, newIndices, 0, nextLayer.length);
			nextLayer = newIndices;
		}

		private void rebase(int newBase) {
			final var growth = base - newBase;

			final var newIndices = new Object[nextLayer.length + growth];
			// Shift the existing elements to the end of the new array to maintain their offset with the new base.
			System.arraycopy(nextLayer, 0, newIndices, growth, nextLayer.length);

			nextLayer = newIndices;
			base = newBase;
		}
	}

	public static final class IntLayer {
		private boolean hasBase = false;
		private int base = 0;
		private int[] indices = new int[0];

		public void fillLut(IntArrayList lut) {
			lut.add(base);
			lut.add(indices.length);

			for (int index : indices) {
				lut.add(index);
			}
		}

		public int base() {
			return base;
		}

		public int size() {
			return indices.length;
		}

		public int getRaw(int i) {
			if (i < 0) {
				return 0;
			}

			if (i >= indices.length) {
				return 0;
			}

			return indices[i];
		}

		public int get(int i) {
			if (!hasBase) {
				return 0;
			}

			return getRaw(i - base);
		}

		public void set(int i, int index) {
			if (!hasBase) {
				base = i;
				hasBase = true;
			}

			if (i < base) {
				rebase(i);
			}

			final var offset = i - base;

			if (offset >= indices.length) {
				resize(offset + 1);
			}

			indices[offset] = index;
		}

		/**
		 * @return {@code true} if the layer is now empty.
		 */
		public boolean prune() {
			if (!hasBase) {
				return true;
			}

			var leadingZeros = getLeadingZeros();

			if (leadingZeros == indices.length) {
				return true;
			}

			var trailingZeros = getTrailingZeros();

			if (leadingZeros == 0 && trailingZeros == 0) {
				return false;
			}

			final var newIndices = new int[indices.length - leadingZeros - trailingZeros];

			System.arraycopy(indices, leadingZeros, newIndices, 0, newIndices.length);
			indices = newIndices;
			base += leadingZeros;

			return false;
		}

		private int getTrailingZeros() {
			int out = 0;

			for (int i = indices.length - 1; i >= 0; i--) {
				if (indices[i] == 0) {
					out++;
				} else {
					break;
				}
			}
			return out;
		}

		private int getLeadingZeros() {
			int out = 0;

			for (int index : indices) {
				if (index == 0) {
					out++;
				} else {
					break;
				}
			}
			return out;
		}

		public void clear(int i) {
			if (!hasBase) {
				return;
			}

			if (i < base) {
				return;
			}

			final var offset = i - base;

			if (offset >= indices.length) {
				return;
			}

			indices[offset] = 0;
		}

		private void resize(int length) {
			final var newIndices = new int[length];
			System.arraycopy(indices, 0, newIndices, 0, indices.length);
			indices = newIndices;
		}

		private void rebase(int newBase) {
			final var growth = base - newBase;

			final var newIndices = new int[indices.length + growth];
			System.arraycopy(indices, 0, newIndices, growth, indices.length);

			indices = newIndices;
			base = newBase;
		}
	}
}
