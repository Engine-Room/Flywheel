package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.jozufozu.flywheel.api.backend.Backend;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class BackendArgument implements ArgumentType<Backend> {
	private static final List<String> STRING_IDS = Backend.REGISTRY.getAllIds().stream().map(ResourceLocation::toString).toList();

	private static final Dynamic2CommandExceptionType INVALID = new Dynamic2CommandExceptionType((found, constants) -> {
		// TODO: don't steal lang
		return new TranslatableComponent("commands.forge.arguments.enum.invalid", constants, found);
	});

	public static final BackendArgument INSTANCE = new BackendArgument();

	@Override
	public Backend parse(StringReader reader) throws CommandSyntaxException {
		ResourceLocation id = ResourceLocation.read(reader);
		Backend backend = Backend.REGISTRY.get(id);

		if (backend == null) {
			throw INVALID.createWithContext(reader, id.toString(), STRING_IDS);
		}

		return backend;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(STRING_IDS, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return STRING_IDS;
	}
}
