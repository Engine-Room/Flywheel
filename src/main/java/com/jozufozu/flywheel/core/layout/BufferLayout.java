package com.jozufozu.flywheel.core.layout;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.VertexAttribute;

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

	private final List<VertexAttribute> attributes;

	private final int stride;

	public BufferLayout(List<LayoutItem> layoutItems, int padding) {

		ImmutableList.Builder<VertexAttribute> attributes = ImmutableList.builder();

		for (LayoutItem item : layoutItems) {
			item.provideAttributes(attributes::add);
		}

		this.attributes = attributes.build();
		this.stride = calculateStride(this.attributes) + padding;
	}

	public Collection<VertexAttribute> getAttributes() {
		return attributes;
	}

	public int getAttributeCount() {
		return attributes.size();
	}

	public int getStride() {
		return stride;
	}

	public static Builder builder() {
		return new Builder();
	}

	private static int calculateStride(List<VertexAttribute> layoutItems) {
		int stride = 0;
		for (VertexAttribute spec : layoutItems) {
			stride += spec.getByteWidth();
		}
		return stride;
	}

	public static class Builder {
		private final ImmutableList.Builder<LayoutItem> allItems;
		private int padding;

		public Builder() {
			allItems = ImmutableList.builder();
		}

		public Builder addItems(LayoutItem... attributes) {
			allItems.add(attributes);
			return this;
		}

		public Builder withPadding(int padding) {
			this.padding = padding;
			return this;
		}

		public BufferLayout build() {
			return new BufferLayout(allItems.build(), padding);
		}
	}

}
