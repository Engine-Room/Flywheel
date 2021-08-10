package com.jozufozu.flywheel.backend.pipeline;

import java.util.Collection;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.backend.source.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.source.parse.StructField;

/**
 * A single input to a shader.
 */
public class ShaderInput {
	public final CharSequence name;
	public final int attribCount;

	public ShaderInput(CharSequence name, int attribCount) {
		this.name = name;
		this.attribCount = attribCount;
	}

	public ShaderInput withPrefix(CharSequence prefix) {
		return new ShaderInput(prefix.toString() + name, attribCount);
	}

	public static Collection<ShaderInput> fromStruct(ShaderStruct struct, String prefix) {
		return struct.getFields()
				.stream()
				.map(ShaderInput::from)
				.map(a -> a.withPrefix(prefix))
				.collect(Collectors.toList());
	}

	public static ShaderInput from(StructField field) {
		int attributeCount = TypeHelper.getAttributeCount(field.type);

		return new ShaderInput(field.name, attributeCount);
	}
}
