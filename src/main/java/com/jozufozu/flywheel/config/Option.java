package com.jozufozu.flywheel.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public interface Option<T> {
	String getKey();

	T get();

	void set(T value);

	JsonElement toJson();

	void fromJson(JsonElement json) throws JsonParseException;

	public abstract class BaseOption<T> implements Option<T> {
		protected String key;
		protected T value;

		public BaseOption(String key, T defaultValue) {
			this.key = key;
			value = defaultValue;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public T get() {
			return value;
		}

		@Override
		public void set(T value) {
			this.value = value;
		}
	}

	public class BooleanOption extends BaseOption<Boolean> {
		public BooleanOption(String id, Boolean defaultValue) {
			super(id, defaultValue);
		}

		@Override
		public JsonElement toJson() {
			return new JsonPrimitive(get());
		}

		@Override
		public void fromJson(JsonElement json) throws JsonParseException {
			set(json.getAsBoolean());
		}
	}

	public class EnumOption<E extends Enum<E>> extends BaseOption<E> {
		protected final Class<E> enumType;

		@SuppressWarnings("unchecked")
		public EnumOption(String id, E defaultValue) {
			super(id, defaultValue);
			enumType = (Class<E>) defaultValue.getClass();
		}

		@Override
		public JsonElement toJson() {
			return new JsonPrimitive(get().name());
		}

		@Override
		public void fromJson(JsonElement json) throws JsonParseException {
			String constantName = json.getAsString();
			for (E constant : enumType.getEnumConstants()) {
				if (constant.name().equals(constantName)) {
					set(constant);
					break;
				}
			}
		}

		public Class<E> getEnumType() {
			return enumType;
		}
	}
}
