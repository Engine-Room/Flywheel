package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.lib.util.ResourceUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.ResourceLocationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BackendArgument implements ArgumentType<Backend> {
	private static final List<String> STRING_IDS = Backend.REGISTRY.getAllIds()
			.stream()
			.map(rl -> {
				if (Flywheel.ID
                        .equals(rl.getNamespace())) {
					return rl.getPath();
				} else {
					return rl.toString();
				}
			})
			.toList();

	private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.id.invalid"));

	public static final DynamicCommandExceptionType ERROR_UNKNOWN_BACKEND = new DynamicCommandExceptionType(arg -> {
		return Component.literal("Unknown backend '" + arg + "'");
	});

	public static final BackendArgument INSTANCE = new BackendArgument();

	@Override
	public Backend parse(StringReader reader) throws CommandSyntaxException {
		ResourceLocation id = getRead(reader);
		Backend backend = Backend.REGISTRY.get(id);

		if (backend == null) {
			throw ERROR_UNKNOWN_BACKEND.createWithContext(reader, id.toString());
		}

		return backend;
	}

	/**
	 * Copied from {@link ResourceLocation#read}, but defaults to flywheel namespace.
	 */
	@NotNull
	private static ResourceLocation getRead(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();

		while(reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
		   reader.skip();
		}

		String s = reader.getString().substring(i, reader.getCursor());

		try {
		   return ResourceUtil.defaultToFlywheelNamespace(s);
		} catch (ResourceLocationException resourcelocationexception) {
		   reader.setCursor(i);
		   throw ERROR_INVALID.createWithContext(reader);
		}
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
