package dev.engine_room.flywheel.backend.engine;

import java.util.function.Supplier;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceHandle;

public class InstanceHandleImpl<I extends Instance> implements InstanceHandle {
	public State<I> state;
	@UnknownNullability
	public I instance;
	public int index;

	public InstanceHandleImpl(State<I> state) {
		this.state = state;
	}

	@Override
	public void setChanged() {
		state = state.setChanged(index);
	}

	@Override
	public void setDeleted() {
		state = state.setDeleted(index);
		// invalidate ourselves
		clear();
	}

	@Override
	public void setVisible(boolean visible) {
		state = state.setVisible(index, instance, visible);
	}

	@Override
	public boolean isVisible() {
		return state.status() == Status.VISIBLE;
	}

	public void clear() {
		index = -1;
	}

	public enum Status {
		HIDDEN,
		DELETED,
		VISIBLE
	}

	public interface State<I extends Instance> {
		State<I> setChanged(int index);

		State<I> setDeleted(int index);

		State<I> setVisible(int index, I instance, boolean visible);

		Status status();
	}

	public record Hidden<I extends Instance>(Supplier<AbstractInstancer<I>> supplier) implements State<I> {
		@Override
		public State<I> setChanged(int index) {
			return this;
		}

		@Override
		public State<I> setDeleted(int index) {
			return this;
		}

		@Override
		public State<I> setVisible(int index, I instance, boolean visible) {
			if (!visible) {
				return this;
			}
			var instancer = supplier.get();
			instancer.stealInstance(instance);
			return instancer;
		}

		@Override
		public Status status() {
			return Status.HIDDEN;
		}
	}

	public record Deleted<I extends Instance>() implements State<I> {
		private static final Deleted<?> INSTANCE = new Deleted<>();

		@SuppressWarnings("unchecked")
		public static <I extends Instance> Deleted<I> instance() {
			return (Deleted<I>) INSTANCE;
		}

		@Override
		public State<I> setChanged(int index) {
			return this;
		}

		@Override
		public State<I> setDeleted(int index) {
			return this;
		}

		@Override
		public State<I> setVisible(int index, I instance, boolean visible) {
			return this;
		}

		@Override
		public Status status() {
			return Status.DELETED;
		}
	}
}
