package com.jozufozu.flywheel.util;

import java.util.regex.Pattern;

import net.minecraft.resources.ResourceLocation;

public class ResourceUtil {
	// Match the complement of alphanumeric and underscore.
	private static final Pattern UNSAFE_CHARS = Pattern.compile("[^a-zA-Z0-9_]");

	public static ResourceLocation defaultToFlywheelNamespace(String location) {
		String[] astring = new String[]{"flywheel", location};
		int i = location.indexOf(':');
		if (i >= 0) {
			astring[1] = location.substring(i + 1);
			if (i >= 1) {
				astring[0] = location.substring(0, i);
			}
		}

		return new ResourceLocation(astring[0], astring[1]);
	}

	public static ResourceLocation subPath(ResourceLocation root, String subPath) {
		return new ResourceLocation(root.getNamespace(), root.getPath() + subPath);
	}

	public static ResourceLocation removePrefixUnchecked(ResourceLocation full, String root) {
		return new ResourceLocation(full.getNamespace(), full.getPath()
				.substring(root.length()));
	}

	public static ResourceLocation trim(ResourceLocation loc, String prefix, String suffix) {
		String path = loc.getPath();
		return new ResourceLocation(loc.getNamespace(), path.substring(prefix.length(), path.length() - suffix.length()));
	}

	public static String toSafeString(ResourceLocation rl) {
		return UNSAFE_CHARS.matcher(rl.toString())
				.replaceAll("_");
	}

	public static ResourceLocation prefixed(String basePath, ResourceLocation resourceLocation) {
		String path = resourceLocation.getPath();
		return new ResourceLocation(resourceLocation.getNamespace(), basePath + path);
	}
}
