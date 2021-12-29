package com.jozufozu.flywheel.core.layout;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;

/**
 * Classic Vertex Format struct with a clever name.
 *
 * <p>
 *     Used for vertices and instances. Describes the layout of a datatype in a buffer object.
 * </p>
 *
 * @see com.jozufozu.flywheel.api.struct.StructType
 * @see VertexType
 */
public class BufferLayout {

	private final List<LayoutItem> allAttributes;

	private final int numAttributes;
	private final int stride;

	public BufferLayout(List<LayoutItem> allAttributes) {
		this.allAttributes = allAttributes;

		int numAttributes = 0, stride = 0;
		for (LayoutItem spec : allAttributes) {
			numAttributes += spec.getAttributeCount();
			stride += spec.getSize();
		}
		this.numAttributes = numAttributes;
		this.stride = stride;
	}

	public List<LayoutItem> getLayoutItems() {
		return allAttributes;
	}

	public int getAttributeCount() {
		return numAttributes;
	}

	public int getStride() {
		return stride;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final ImmutableList.Builder<LayoutItem> allItems;

		public Builder() {
			allItems = ImmutableList.builder();
		}

		public Builder addItems(LayoutItem... attributes) {
			allItems.add(attributes);
			return this;
		}

		public BufferLayout build() {
			return new BufferLayout(allItems.build());
		}
	}
}
