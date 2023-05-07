package com.jozufozu.flywheel.api.layout;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.gl.array.VertexAttribute;

/**
 * Classic Vertex Format with a clever name.
 *
 * <p>
 *     Used for vertices and instances. Describes the layout of a datatype in a buffer object.
 * </p>
 *
 * @see com.jozufozu.flywheel.api.instance.InstanceType
 * @see VertexType
 */
public class BufferLayout {

	public final List<LayoutItem> layoutItems;
	public final List<VertexAttribute> attributes;

	private final int stride;

	public BufferLayout(List<LayoutItem> layoutItems, int padding) {
		this.layoutItems = ImmutableList.copyOf(layoutItems);

		ImmutableList.Builder<VertexAttribute> attributes = ImmutableList.builder();

		for (var item : this.layoutItems) {
			item.type()
					.provideAttributes(attributes::add);
		}

		this.attributes = attributes.build();
		this.stride = calculateStride(this.attributes) + padding;
	}

	public List<VertexAttribute> attributes() {
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
		for (var spec : layoutItems) {
			stride += spec.byteWidth();
		}
		return stride;
	}

	public static class Builder {
		private final ImmutableList.Builder<LayoutItem> allItems;
		private int padding;

		public Builder() {
			allItems = ImmutableList.builder();
		}

		public Builder addItem(InputType type, String name) {
			allItems.add(new LayoutItem(type, name));
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
