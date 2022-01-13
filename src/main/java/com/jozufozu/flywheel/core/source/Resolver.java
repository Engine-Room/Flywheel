package com.jozufozu.flywheel.core.source;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

/**
 * Manages deferred file resolution.
 *
 * <p>
 *     Interns all file names in shader sources and program specs, deduplicating the final lookups and allowing for more
 *     dev-friendly error reporting.
 * </p>
 *
 * @see FileResolution
 */
public class Resolver {

	public static final Resolver INSTANCE = new Resolver();

	private final Map<ResourceLocation, FileResolution> resolutions = new HashMap<>();
	private boolean hasRun = false;

	public FileResolution get(ResourceLocation file) {
		if (!hasRun) {
			return resolutions.computeIfAbsent(file, FileResolution::new);
		} else {
			// Lock the map after resolution has run.
			FileResolution fileResolution = resolutions.get(file);

			// ...so crash immediately if the file isn't found.
			if (fileResolution == null) {
				throw new RuntimeException("could not find source for file: " + file);
			}

			return fileResolution;
		}
	}

	/**
	 * Try and resolve all referenced source files, printing errors if any aren't found.
	 */
	public void run(SourceFinder sources) {
		boolean needsCrash = false;
		for (FileResolution resolution : resolutions.values()) {
			if (!resolution.resolve(sources)) {
				needsCrash = true;
			}
		}

		if (needsCrash) {
			throw new ShaderLoadingException("Failed to resolve all source files, see log for details");
		}

		hasRun = true;
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
		hasRun = false;
	}
}
