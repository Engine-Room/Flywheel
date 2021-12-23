package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;

public class EngineArgument implements ArgumentType<FlwEngine> {

	public static final EngineArgument INSTANCE = new EngineArgument();
	public static final Serializer SERIALIZER = new Serializer();

	private static final Dynamic2CommandExceptionType INVALID = new Dynamic2CommandExceptionType((found, constants) -> {
		return new TranslatableComponent("commands.forge.arguments.enum.invalid", constants, found);
	});

	private EngineArgument() {
	}

	@Override
	public FlwEngine parse(StringReader reader) throws CommandSyntaxException {
		String string = reader.readUnquotedString();

		FlwEngine engine = FlwEngine.byName(string);

		if (engine == null) {
			throw INVALID.createWithContext(reader, string, FlwEngine.validNames());
		}

		return engine;
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(FlwEngine.validNames(), builder);
	}

	@Override
	public Collection<String> getExamples() {
		return FlwEngine.validNames();
	}

	public static class Serializer implements ArgumentSerializer<EngineArgument> {
		private Serializer() {
		}

		public void serializeToNetwork(EngineArgument argument, FriendlyByteBuf buffer) {
		}

		public EngineArgument deserializeFromNetwork(FriendlyByteBuf buffer) {
			return INSTANCE;
		}

		public void serializeToJson(EngineArgument argument, JsonObject json) {
		}
	}
}
