package dev.engine_room.flywheel.backend.engine;

import java.util.function.Supplier;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceHandle;

public class InstanceHandleImpl<I extends Instance> implements InstanceHandle {
	@UnknownNullability
	public AbstractInstancer<I> instancer;
	@UnknownNullability
	public I instance;
	@UnknownNullability
	public Supplier<AbstractInstancer<I>> recreate;
	public boolean visible = true;
	public int index;

	@Override
	public void setChanged() {
		instancer.notifyDirty(index);
	}

	@Override
	public void setDeleted() {
		instancer.notifyRemoval(index);
		// invalidate ourselves
		clear();
	}

	@Override
	public void setVisible(boolean visible) {
		if (this.visible == visible) {
			return;
		}

		this.visible = visible;

		if (visible) {
			recreate.get().stealInstance(instance);
		} else {
			instancer.notifyRemoval(index);
			clear();
		}
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public void clear() {
		index = -1;
	}
}
