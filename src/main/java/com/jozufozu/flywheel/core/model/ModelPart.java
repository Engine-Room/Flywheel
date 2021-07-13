package com.jozufozu.flywheel.core.model;

public class ModelPart {

	public static PartBuilder builder(int sizeU, int sizeV) {
		return new PartBuilder(sizeU, sizeV);
	}
}
