package com.jozufozu.flywheel.lib.util;

import com.jozufozu.flywheel.Flywheel;

import net.minecraft.resources.ResourceLocation;

public class ResourceUtil {
	public static ResourceLocation defaultToFlywheelNamespace(String location) {
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
}
