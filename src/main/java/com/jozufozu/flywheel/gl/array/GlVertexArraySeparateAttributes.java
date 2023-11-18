package com.jozufozu.flywheel.gl.array;

import java.util.BitSet;
import java.util.List;

import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.Checks;

import com.jozufozu.flywheel.gl.GlCompat;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.lib.util.FlwUtil;

public class GlVertexArraySeparateAttributes extends GlVertexArray {
	public static final boolean SUPPORTED = isSupported();
	private final BitSet attributeEnabled = new BitSet(MAX_ATTRIBS);
	private final VertexAttribute[] attributes = new VertexAttribute[MAX_ATTRIBS];
	private final int[] attributeBindings = FlwUtil.initArray(MAX_ATTRIBS, -1);
	private final int[] bindingBuffers = new int[MAX_ATTRIB_BINDINGS];
	private final long[] bindingOffsets = new long[MAX_ATTRIB_BINDINGS];
	private final int[] bindingStrides = new int[MAX_ATTRIB_BINDINGS];
	private final int[] bindingDivisors = new int[MAX_ATTRIB_BINDINGS];

	private int elementBufferBinding = 0;

	public GlVertexArraySeparateAttributes() {
		handle(GL43C.glGenVertexArrays());
	}

	@Override
	public void bindVertexBuffer(final int bindingIndex, final int vbo, final long offset, final int stride) {
		if (bindingBuffers[bindingIndex] != vbo || bindingOffsets[bindingIndex] != offset || bindingStrides[bindingIndex] != stride) {
			GlStateTracker.bindVao(handle());
			GL43C.glBindVertexBuffer(bindingIndex, vbo, offset, stride);
			bindingBuffers[bindingIndex] = vbo;
			bindingOffsets[bindingIndex] = offset;
			bindingStrides[bindingIndex] = stride;
		}
	}

	@Override
	public void setBindingDivisor(final int bindingIndex, final int divisor) {
		if (bindingDivisors[bindingIndex] != divisor) {
			GL43C.glVertexBindingDivisor(bindingIndex, divisor);
			bindingDivisors[bindingIndex] = divisor;
		}
	}

	@Override
	public void bindAttributes(final int bindingIndex, final int startAttribIndex, List<VertexAttribute> vertexAttributes) {
		GlStateTracker.bindVao(handle());
		int attribIndex = startAttribIndex;
		int offset = 0;

		for (var attribute : vertexAttributes) {
			if (!attributeEnabled.get(attribIndex)) {
				GL43C.glEnableVertexAttribArray(attribIndex);
				attributeEnabled.set(attribIndex);
			}

			if (!attribute.equals(attributes[attribIndex])) {
				if (attribute instanceof VertexAttribute.Float f) {
					GL43C.glVertexAttribFormat(attribIndex, f.size(), f.type().glEnum, f.normalized(), offset);
				} else if (attribute instanceof VertexAttribute.Int vi) {
					GL43C.glVertexAttribIFormat(attribIndex, vi.size(), vi.type().glEnum, offset);
				}
				attributes[attribIndex] = attribute;
			}

			if (attributeBindings[attribIndex] != bindingIndex) {
				GL43C.glVertexAttribBinding(attribIndex, bindingIndex);
				attributeBindings[attribIndex] = bindingIndex;
			}

			attribIndex++;
			offset += attribute.byteWidth();
		}
	}

	@Override
	public void setElementBuffer(int ebo) {
		if (elementBufferBinding != ebo) {
			GlStateTracker.bindVao(handle());
			GlBufferType.ELEMENT_ARRAY_BUFFER.bind(ebo);
			elementBufferBinding = ebo;
		}
	}

	private static boolean isSupported() {
		var c = GlCompat.CAPABILITIES;
		return GlCompat.ALLOW_DSA && Checks.checkFunctions(c.glBindVertexBuffer, c.glVertexBindingDivisor, c.glEnableVertexAttribArray, c.glVertexAttribFormat, c.glVertexAttribIFormat, c.glVertexAttribBinding);
	}
}
