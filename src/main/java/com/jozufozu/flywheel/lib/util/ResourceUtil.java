package com.jozufozu.flywheel.lib.util;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.Flywheel;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ResourceUtil {
	private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.id.invalid"));

	private ResourceUtil() {
	}

	/**
	 * Same as {@link ResourceLocation#ResourceLocation(String)}, but defaults to Flywheel namespace.
	 */
	public static ResourceLocation parseFlywheelDefault(String location) {
		String namespace = Flywheel.ID;
		String path = location;

		int i = location.indexOf(ResourceLocation.NAMESPACE_SEPARATOR);
		if (i >= 0) {
			path = location.substring(i + 1);
			if (i >= 1) {
				namespace = location.substring(0, i);
			}
		}

		return new ResourceLocation(namespace, path);
	}

	/**
	 * Same as {@link ResourceLocation#read(StringReader)}, but defaults to Flywheel namespace.
	 */
	public static ResourceLocation readFlywheelDefault(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();

		while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
		   reader.skip();
		}

		String s = reader.getString().substring(i, reader.getCursor());

		try {
		   return parseFlywheelDefault(s);
		} catch (ResourceLocationException resourcelocationexception) {
		   reader.setCursor(i);
		   throw ERROR_INVALID.createWithContext(reader);
		}
	}

	@NotNull
	public static String toDebugFileNameNoExtension(ResourceLocation resourceLocation) {
		var stringLoc = resourceLocation.toString();
		return stringLoc.substring(0, stringLoc.lastIndexOf('.'))
				.replace('/', '_')
				.replace(':', '_');
	}
}
