package com.jozufozu.flywheel.core.source;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;

import net.minecraft.resources.ResourceLocation;

/**
 * Indexes many shader source definitions to allow for error fix suggestions.
 */
public class Index {

	private final Multimap<String, ShaderStruct> knownStructs = MultimapBuilder.hashKeys()
			.hashSetValues()
			.build();

	private final Multimap<String, ShaderFunction> knownFunctions = MultimapBuilder.hashKeys()
			.hashSetValues()
			.build();

	public Index(Map<ResourceLocation, SourceFile> sources) {
		Collection<SourceFile> files = sources.values();

		for (SourceFile file : files) {
            file.structs.forEach(knownStructs::put);
			file.functions.forEach(knownFunctions::put);
		}
	}

	public Collection<ShaderStruct> getStructDefinitionsMatching(CharSequence name) {
		return knownStructs.get(name.toString());
	}

	public Collection<ShaderFunction> getFunctionDefinitionsMatching(CharSequence name) {
		return knownFunctions.get(name.toString());
	}
}
