package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.lib.util.ResourceUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BackendArgument implements ArgumentType<Backend> {
	private static final List<String> EXAMPLES = List.of("off", "flywheel:off", "instancing");

	private static final DynamicCommandExceptionType ERROR_UNKNOWN_BACKEND = new DynamicCommandExceptionType(arg -> {
		return Component.translatable("argument.flywheel_backend.id.unknown", arg);
	});

	public static final BackendArgument INSTANCE = new BackendArgument();
	public static final SingletonArgumentInfo<BackendArgument> INFO = SingletonArgumentInfo.contextFree(() -> INSTANCE);

	@Override
	public Backend parse(StringReader reader) throws CommandSyntaxException {
		ResourceLocation id = ResourceUtil.readFlywheelDefault(reader);
		Backend backend = Backend.REGISTRY.get(id);

		if (backend == null) {
			throw ERROR_UNKNOWN_BACKEND.createWithContext(reader, id.toString());
		}

		return backend;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		String input = builder.getRemaining().toLowerCase(Locale.ROOT);
		for (ResourceLocation id : Backend.REGISTRY.getAllIds()) {
			String idStr = id.toString();
			if (SharedSuggestionProvider.matchesSubStr(input, idStr) || SharedSuggestionProvider.matchesSubStr(input, id.getPath())) {
				builder.suggest(idStr);
			}
		}
		return builder.buildFuture();
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
