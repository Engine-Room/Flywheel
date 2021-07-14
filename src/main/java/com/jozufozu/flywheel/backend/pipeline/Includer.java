package com.jozufozu.flywheel.backend.pipeline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.pipeline.parse.Include;

import net.minecraft.util.ResourceLocation;

public class Includer {

	public static List<SourceFile> recurseIncludes(SourceFile from) {
		Set<ResourceLocation> seen = new HashSet<>();

		seen.add(from.name);

		List<SourceFile> out = new ArrayList<>();

		process(seen, out, from);

		return out;
	}

	private static void process(Set<ResourceLocation> seen, List<SourceFile> out, SourceFile source) {
		ImmutableList<Include> includes = source.getIncludes();

		for (Include include : includes) {

			SourceFile file = include.getTarget();
			if (file != null && seen.add(file.name)) {
				process(seen, out, file);

				out.add(file);
			}
		}
	}
}
