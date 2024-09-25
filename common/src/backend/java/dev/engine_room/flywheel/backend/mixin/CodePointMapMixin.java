package dev.engine_room.flywheel.backend.mixin;

import java.util.Arrays;
import java.util.function.IntFunction;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.gui.font.CodepointMap;

@Mixin(CodepointMap.class)
public class CodePointMapMixin<T> {
	@Shadow
	@Final
	private T[][] blockMap;

	@Shadow
	@Final
	private T[] empty;

	@Shadow
	@Final
	private IntFunction<T[]> blockConstructor;

	@Unique
	private final Object flywheel$lock = new Object();

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void clear() {
		synchronized (flywheel$lock) {
			Arrays.fill(this.blockMap, this.empty);
		}
	}

	/**
	 * @author
	 * @reason
	 */
	@Nullable
	@Overwrite
	public T get(int index) {
		int i = index >> 8;
		int j = index & 0xFF;
		synchronized (flywheel$lock) {
			return this.blockMap[i][j];
		}
	}

	/**
	 * @author
	 * @reason
	 */
	@Nullable
	@Overwrite
	public T put(int index, T value) {
		int i = index >> 8;
		int j = index & 0xFF;
		T object;
		synchronized (flywheel$lock) {
			T[] objects = this.blockMap[i];
			if (objects == this.empty) {
				objects = this.blockConstructor.apply(256);
				this.blockMap[i] = objects;
				objects[j] = value;
				return null;
			}
			object = objects[j];
			objects[j] = value;
		}
		return object;
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public T computeIfAbsent(int index, IntFunction<T> valueIfAbsentGetter) {
		int i = index >> 8;
		int j = index & 0xFF;
		T out;
		synchronized (flywheel$lock) {
			T[] objects = this.blockMap[i];
			T object = objects[j];
			if (object != null) {
				return object;
			}
			if (objects == this.empty) {
				objects = this.blockConstructor.apply(256);
				this.blockMap[i] = objects;
			}
			out = valueIfAbsentGetter.apply(index);
			objects[j] = out;
		}
		return out;
	}

	/**
	 * @author
	 * @reason
	 */
	@Nullable
	@Overwrite
	public T remove(int index) {
		int i = index >> 8;
		int j = index & 0xFF;
		T object;
		synchronized (flywheel$lock) {
			T[] objects = this.blockMap[i];
			if (objects == this.empty) {
				return null;
			}
			object = objects[j];
			objects[j] = null;
		}
		return object;
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void forEach(CodepointMap.Output<T> output) {
		synchronized (flywheel$lock) {
			for (int i = 0; i < this.blockMap.length; ++i) {
				T[] objects = this.blockMap[i];
				if (objects == this.empty) continue;
				for (int j = 0; j < objects.length; ++j) {
					T object = objects[j];
					if (object == null) continue;
					int k = i << 8 | j;
					output.accept(k, object);
				}
			}
		}
	}
}
