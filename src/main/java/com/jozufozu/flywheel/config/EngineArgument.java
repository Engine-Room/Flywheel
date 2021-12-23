package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;

public class EngineArgument implements ArgumentType<FlwEngine> {

	private static final EngineArgument INSTANCE = new EngineArgument();

	private static final Dynamic2CommandExceptionType INVALID = new Dynamic2CommandExceptionType((found, constants) -> {
		// TODO: don't steal lang
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

	public static EngineArgument getInstance() {
		return INSTANCE;
	}
}
