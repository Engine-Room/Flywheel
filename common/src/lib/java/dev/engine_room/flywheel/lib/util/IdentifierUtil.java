package dev.engine_room.flywheel.lib.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import dev.engine_room.flywheel.api.Flywheel;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class IdentifierUtil {
	private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.id.invalid"));

	private IdentifierUtil() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(Flywheel.ID, path);
	}

	/**
	 * Same as {@link Identifier#parse(String)}, but defaults to Flywheel namespace.
	 */
	public static Identifier parseFlywheelDefault(String id) {
		String namespace = Flywheel.ID;
		String path = id;

		int i = id.indexOf(Identifier.NAMESPACE_SEPARATOR);
		if (i >= 0) {
			path = id.substring(i + 1);
			if (i >= 1) {
				namespace = id.substring(0, i);
			}
		}

		return Identifier.fromNamespaceAndPath(namespace, path);
	}

	/**
	 * Same as {@link Identifier#read(StringReader)}, but defaults to Flywheel namespace.
	 */
	public static Identifier readFlywheelDefault(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();

		while (reader.canRead() && Identifier.isAllowedInIdentifier(reader.peek())) {
		   reader.skip();
		}

		String s = reader.getString().substring(i, reader.getCursor());

		try {
		   return parseFlywheelDefault(s);
		} catch (IdentifierException e) {
		   reader.setCursor(i);
		   throw ERROR_INVALID.createWithContext(reader);
		}
	}

	/**
	 * Same as {@link Identifier#toDebugFileName()}, but also removes the file extension.
	 */
	public static String toDebugFileNameNoExtension(Identifier id) {
		var stringLoc = id.toDebugFileName();
		return stringLoc.substring(0, stringLoc.lastIndexOf('.'));
	}
}
