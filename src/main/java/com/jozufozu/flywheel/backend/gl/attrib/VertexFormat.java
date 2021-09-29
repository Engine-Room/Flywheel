package com.jozufozu.flywheel.backend.gl.attrib;

import java.util.ArrayList;
import java.util.Collections;

public class VertexFormat {

	private final ArrayList<AttribSpec> allAttributes;

	private final int numAttributes;
	private final int stride;

	public VertexFormat(ArrayList<AttribSpec> allAttributes) {
		this.allAttributes = allAttributes;

		int numAttributes = 0, stride = 0;
		for (AttribSpec spec : allAttributes) {
			numAttributes += spec.getAttributeCount();
			stride += spec.getSize();
		}
		this.numAttributes = numAttributes;
		this.stride = stride;
	}

	public int getAttributeCount() {
		return numAttributes;
	}

	public int getStride() {
		return stride;
	}

	public void vertexAttribPointers(int index) {
		int offset = 0;
		for (AttribSpec spec : this.allAttributes) {
			spec.vertexAttribPointer(stride, index, offset);
			index += spec.getAttributeCount();
			offset += spec.getSize();
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final ArrayList<AttribSpec> allAttributes = new ArrayList<>();

		public Builder() {
		}

		public Builder addAttributes(AttribSpec... attributes) {
			Collections.addAll(allAttributes, attributes);
			return this;
		}

		public VertexFormat build() {
			return new VertexFormat(allAttributes);
		}
	}
}
