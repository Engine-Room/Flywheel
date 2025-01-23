package dev.engine_room.flywheel.impl.mixin.text;

import java.util.function.IntFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.gui.font.CodepointMap;

@Mixin(CodepointMap.class)
public class CodePointMapMixin<T> {
	@Unique
	private final Object flywheel$lock = new Object();

	@WrapMethod(method = "clear")
	private void flywheel$wrapClearAsSynchronized(Operation<Void> original) {
		synchronized (flywheel$lock) {
			original.call();
		}
	}

	@WrapMethod(method = "get")
	private T flywheel$wrapGetAsSynchronized(int index, Operation<T> original) {
		synchronized (flywheel$lock) {
			return original.call(index);
		}
	}

	@WrapMethod(method = "put")
	private T flywheel$wrapPutAsSynchronized(int index, T value, Operation<T> original) {
		synchronized (flywheel$lock) {
			return original.call(index, value);
		}
	}

	@WrapMethod(method = "computeIfAbsent")
	private T flywheel$wrapComputeIfAbsentAsSynchronized(int index, IntFunction<T> valueIfAbsentGetter, Operation<T> original) {
		synchronized (flywheel$lock) {
			return original.call(index, valueIfAbsentGetter);
		}
	}

	@WrapMethod(method = "remove")
	private T flywheel$wrapRemoveAsSynchronized(int index, Operation<T> original) {
		synchronized (flywheel$lock) {
			return original.call(index);
		}
	}

	@WrapMethod(method = "forEach")
	private void flywheel$wrapForEachAsSynchronized(CodepointMap.Output<T> output, Operation<Void> original) {
		synchronized (flywheel$lock) {
			original.call(output);
		}
	}
}
