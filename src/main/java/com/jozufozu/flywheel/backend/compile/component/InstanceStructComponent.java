package com.jozufozu.flywheel.backend.compile.component;

import java.util.Collection;
import java.util.Collections;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.ElementType;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.layout.MatrixElementType;
import com.jozufozu.flywheel.api.layout.ScalarElementType;
import com.jozufozu.flywheel.api.layout.UnsignedIntegerRepr;
import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBuilder;

public class InstanceStructComponent implements SourceComponent {
	private static final String STRUCT_NAME = "FlwInstance";

	private final Layout layout;

	public InstanceStructComponent(InstanceType<?> type) {
		layout = type.layout();
	}

	@Override
	public String name() {
		return Flywheel.rl("instance_struct").toString();
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return Collections.emptyList();
	}

	@Override
	public String source() {
		var builder = new GlslBuilder();

		var instance = builder.struct();
		instance.setName(STRUCT_NAME);
		for (var element : layout.elements()) {
			instance.addField(typeName(element.type()), element.name());
		}

		builder.blankLine();
		return builder.build();
	}

	private static String typeName(ElementType type) {
		if (type instanceof ScalarElementType scalar) {
			return scalarTypeName(scalar.repr());
		} else if (type instanceof VectorElementType vector) {
			return vectorTypeName(vector.repr(), vector.size());
		} else if (type instanceof MatrixElementType matrix) {
			return matrixTypeName(matrix);
		}

		throw new IllegalArgumentException("Unknown type " + type);
	}

	private static String scalarTypeName(ValueRepr repr) {
		if (repr instanceof IntegerRepr) {
			return "int";
		} else if (repr instanceof UnsignedIntegerRepr) {
			return "uint";
		} else if (repr instanceof FloatRepr) {
			return "float";
		}
		throw new IllegalArgumentException("Unknown repr " + repr);
	}

	private static String vectorTypeName(ValueRepr repr, int size) {
		if (repr instanceof IntegerRepr) {
			return "ivec" + size;
		} else if (repr instanceof UnsignedIntegerRepr) {
			return "uvec" + size;
		} else if (repr instanceof FloatRepr) {
			return "vec" + size;
		}
		throw new IllegalArgumentException("Unknown repr " + repr);
	}

	private static String matrixTypeName(MatrixElementType matrix) {
		return "mat" + matrix.columns() + "x" + matrix.rows();
	}
}
