package com.jozufozu.flywheel.backend.source;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

/**
 * Manages deferred file resolution.
 *
 * <p>
 *     Interns all referenced files in all sources, duplicating the final lookups and allowing for more dev-friendly
 *     error reporting.
 * </p><p>
 *     See {@link FileResolution} for more information.
 * </p>
 */
public class Resolver {

	public static final Resolver INSTANCE = new Resolver();

	private final Map<ResourceLocation, FileResolution> resolutions = new HashMap<>();

	public FileResolution findShader(ResourceLocation fileLoc) {
		return resolutions.computeIfAbsent(fileLoc, FileResolution::new);
	}

	/**
	 * Try and resolve all referenced source files, printing errors if any aren't found.
	 */
	public void resolve(ISourceHolder sources) {
		for (FileResolution resolution : resolutions.values()) {
			resolution.resolve(sources);
		}
	}

	/**
	 * Invalidates all FileResolutions.
	 *
	 * <p>
	 *     Called on resource reload.
	 * </p>
	 */
	public void invalidate() {
		resolutions.values().forEach(FileResolution::invalidate);
	}
}
