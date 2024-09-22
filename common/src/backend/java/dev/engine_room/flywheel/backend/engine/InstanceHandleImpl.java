package dev.engine_room.flywheel.backend.engine;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceHandle;

public class InstanceHandleImpl<I extends Instance> implements InstanceHandle {
	public State<I> state;
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
		state = state.setVisible(this, index, visible);
	}

	@Override
	public boolean isVisible() {
		return state instanceof AbstractInstancer<?>;
	}

	public void clear() {
		index = -1;
	}

	public interface State<I extends Instance> {
		State<I> setChanged(int index);

		State<I> setDeleted(int index);

		State<I> setVisible(InstanceHandleImpl<I> handle, int index, boolean visible);
	}

	public record Hidden<I extends Instance>(AbstractInstancer.Recreate<I> recreate, I instance) implements State<I> {
		@Override
		public State<I> setChanged(int index) {
			return this;
		}

		@Override
		public State<I> setDeleted(int index) {
			return this;
		}

		@Override
		public State<I> setVisible(InstanceHandleImpl<I> handle, int index, boolean visible) {
			if (!visible) {
				return this;
			}
			var instancer = recreate.recreate();
			instancer.revealInstance(handle, instance);
			return instancer;
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
		public State<I> setVisible(InstanceHandleImpl<I> handle, int index, boolean visible) {
			return this;
		}
	}
}
