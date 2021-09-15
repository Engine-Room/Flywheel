package com.jozufozu.flywheel.util;

import net.minecraft.resources.ResourceLocation;

public class ResourceUtil {

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
}
