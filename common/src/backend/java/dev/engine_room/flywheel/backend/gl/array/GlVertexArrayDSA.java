package dev.engine_room.flywheel.backend.gl.array;

import java.util.BitSet;
import java.util.List;

import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.Checks;

import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.lib.util.FlwUtil;

public class GlVertexArrayDSA extends GlVertexArray {
	public static final boolean SUPPORTED = isSupported();
	private final BitSet attributeEnabled = new BitSet(MAX_ATTRIBS);
	private final VertexAttribute[] attributes = new VertexAttribute[MAX_ATTRIBS];
	private final int[] attributeBindings = FlwUtil.initArray(MAX_ATTRIBS, -1);
	private final int[] bindingBuffers = new int[MAX_ATTRIB_BINDINGS];
	private final long[] bindingOffsets = new long[MAX_ATTRIB_BINDINGS];
	private final int[] bindingStrides = new int[MAX_ATTRIB_BINDINGS];
	private final int[] bindingDivisors = new int[MAX_ATTRIB_BINDINGS];

	private int elementBufferBinding = 0;

	public GlVertexArrayDSA() {
		handle(GL45C.glCreateVertexArrays());
	}

	@Override
	public void bindVertexBuffer(final int bindingIndex, final int vbo, final long offset, final int stride) {
		if (bindingBuffers[bindingIndex] != vbo || bindingOffsets[bindingIndex] != offset || bindingStrides[bindingIndex] != stride) {
			GL45C.glVertexArrayVertexBuffer(handle(), bindingIndex, vbo, offset, stride);
			bindingBuffers[bindingIndex] = vbo;
			bindingOffsets[bindingIndex] = offset;
			bindingStrides[bindingIndex] = stride;
		}
	}

	@Override
	public void setBindingDivisor(final int bindingIndex, final int divisor) {
		if (bindingDivisors[bindingIndex] != divisor) {
			GL45C.glVertexArrayBindingDivisor(handle(), bindingIndex, divisor);
			bindingDivisors[bindingIndex] = divisor;
		}
	}

	@Override
	public void bindAttributes(final int bindingIndex, final int startAttribIndex, List<VertexAttribute> vertexAttributes) {
		final int handle = handle();
		int attribIndex = startAttribIndex;
		int offset = 0;

		for (var attribute : vertexAttributes) {
			if (!attributeEnabled.get(attribIndex)) {
				GL45C.glEnableVertexArrayAttrib(handle, attribIndex);
				attributeEnabled.set(attribIndex);
			}

			if (!attribute.equals(attributes[attribIndex])) {
				if (attribute instanceof VertexAttribute.Float f) {
					GL45C.glVertexArrayAttribFormat(handle, attribIndex, f.size(), f.type().glEnum, f.normalized(), offset);
				} else if (attribute instanceof VertexAttribute.Int vi) {
					GL45C.glVertexArrayAttribIFormat(handle, attribIndex, vi.size(), vi.type().glEnum, offset);
				}
				attributes[attribIndex] = attribute;
			}

			if (attributeBindings[attribIndex] != bindingIndex) {
				GL45C.glVertexArrayAttribBinding(handle, attribIndex, bindingIndex);
				attributeBindings[attribIndex] = bindingIndex;
			}

			attribIndex++;
			offset += attribute.byteWidth();
		}
	}

	@Override
	public void setElementBuffer(int ebo) {
		if (elementBufferBinding != ebo) {
			GL45C.glVertexArrayElementBuffer(handle(), ebo);
			elementBufferBinding = ebo;
		}
	}

	private static boolean isSupported() {
		var c = GlCompat.CAPABILITIES;
		return GlCompat.ALLOW_DSA && Checks.checkFunctions(c.glCreateVertexArrays, c.glVertexArrayElementBuffer, c.glVertexArrayVertexBuffer, c.glVertexArrayBindingDivisor, c.glVertexArrayAttribBinding, c.glEnableVertexArrayAttrib, c.glVertexArrayAttribFormat, c.glVertexArrayAttribIFormat);
	}
}
