package com.jozufozu.flywheel.lib.layout;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.layout.Element;
import com.jozufozu.flywheel.api.layout.FloatType;
import com.jozufozu.flywheel.api.layout.IntegerType;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.layout.MatrixSize;
import com.jozufozu.flywheel.api.layout.VectorSize;

public class LayoutBuilder {
	private final List<Element> elements = new ArrayList<>();

	public static LayoutBuilder of() {
		return new LayoutBuilder();
	}

	public Layout build() {
		return new Layout(ImmutableList.copyOf(elements));
	}

	public LayoutBuilder element(Element element) {
		elements.add(element);
		return this;
	}

	public LayoutBuilder integer(String name, IntegerType type, VectorSize size) {
		return element(new Element.IntegerVector(name, type, size));
	}

	public LayoutBuilder normalized(String name, IntegerType type, VectorSize size) {
		return element(new Element.NormalizedVector(name, type, size));
	}

	public LayoutBuilder vector(String name, FloatType type, VectorSize size) {
		return element(new Element.Vector(name, type, size));
	}

	public LayoutBuilder mat(String name, MatrixSize rows, MatrixSize cols) {
		return element(new Element.Matrix(name, rows, cols));
	}

	public LayoutBuilder mat(String name, MatrixSize size) {
		return mat(name, size, size);
	}
}
