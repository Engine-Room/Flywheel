package dev.engine_room.flywheel.impl.visualization.storage;

public record Transaction<T>(T obj, Action action) {
	public static <T> Transaction<T> add(T obj) {
		return new Transaction<>(obj, Action.ADD);
	}

	public static <T> Transaction<T> remove(T obj) {
		return new Transaction<>(obj, Action.REMOVE);
	}

	public static <T> Transaction<T> update(T obj) {
		return new Transaction<>(obj, Action.UPDATE);
	}

	public void apply(Storage<T> storage, float partialTick) {
		switch (action) {
		case ADD -> storage.add(obj, partialTick);
		case REMOVE -> storage.remove(obj);
		case UPDATE -> storage.update(obj, partialTick);
		}
	}
}
