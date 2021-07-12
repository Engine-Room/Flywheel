package com.jozufozu.flywheel.config;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

public interface Option<T> {
	String getId();

	T get();

	void set(T value);

	JsonElement toJson() throws IOException;

	void fromJson(JsonElement json) throws IOException;

	void buildCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder);

	public abstract class BaseOption<T> implements Option<T> {
		protected String id;
		protected T value;

		public BaseOption(String id, T defaultValue) {
			this.id = id;
			value = defaultValue;
		}

		@Override
		public String getId() {
			return id;
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
		private final Consumer<FabricClientCommandSource> displayAction;
		private final BiConsumer<FabricClientCommandSource, Boolean> setAction;

		public BooleanOption(String id, Boolean defaultValue, Consumer<FabricClientCommandSource> displayAction, BiConsumer<FabricClientCommandSource, Boolean> setAction) {
			super(id, defaultValue);
			this.displayAction = displayAction;
			this.setAction = setAction;
		}

		@Override
		public JsonElement toJson() throws IOException {
			return new JsonPrimitive(get());
		}

		@Override
		public void fromJson(JsonElement json) throws IOException {
			set(json.getAsBoolean());
		}

		@Override
		public void buildCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
			builder
				.executes(context -> {
					displayAction.accept(context.getSource());
					return Command.SINGLE_SUCCESS;
				}
				).then(ClientCommandManager.literal("on")
					.executes(context -> {
						set(true);
						setAction.accept(context.getSource(), true);
						return Command.SINGLE_SUCCESS;
					})
				).then(ClientCommandManager.literal("off")
					.executes(context -> {
						set(false);
						setAction.accept(context.getSource(), false);
						return Command.SINGLE_SUCCESS;
					})
				);
		}
	}
}
