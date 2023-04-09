package com.jozufozu.flywheel.impl.visualization.storage;

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

	public void apply(Storage<T> storage) {
		switch (action) {
		case ADD -> storage.add(obj);
		case REMOVE -> storage.remove(obj);
		case UPDATE -> storage.update(obj);
		}
	}
}
