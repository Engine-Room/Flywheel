package com.jozufozu.flywheel.gl.array;

import java.util.BitSet;
import java.util.List;

import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.Checks;

import com.jozufozu.flywheel.gl.GlCompat;
import com.jozufozu.flywheel.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.util.FlwUtil;

public abstract class GlVertexArrayGL3 extends GlVertexArray {
	private final BitSet attributeDirty = new BitSet(MAX_ATTRIBS);
	private final int[] attributeOffsets = new int[MAX_ATTRIBS];
	private final VertexAttribute[] attributes = new VertexAttribute[MAX_ATTRIBS];
	private final int[] attributeBindings = FlwUtil.initArray(MAX_ATTRIBS, -1);
	private final int[] bindingBuffers = new int[MAX_ATTRIB_BINDINGS];
	private final long[] bindingOffsets = new long[MAX_ATTRIB_BINDINGS];
	private final int[] bindingStrides = new int[MAX_ATTRIB_BINDINGS];
	private final int[] bindingDivisors = new int[MAX_ATTRIB_BINDINGS];
	private int elementBufferBinding = 0;

	public GlVertexArrayGL3() {
		handle(GL30.glGenVertexArrays());
	}

	@Override
	public void bindForDraw() {
		super.bindForDraw();

		for (int attribIndex = attributeDirty.nextSetBit(0); attribIndex < MAX_ATTRIB_BINDINGS && attribIndex >= 0; attribIndex = attributeDirty.nextSetBit(attribIndex + 1)) {

			int bindingIndex = attributeBindings[attribIndex];
			var attribute = attributes[attribIndex];

			if (bindingIndex == -1 || attribute == null) {
				continue;
			}

			GlBufferType.ARRAY_BUFFER.bind(bindingBuffers[bindingIndex]);
			GL20C.glEnableVertexAttribArray(attribIndex);

			long offset = bindingOffsets[bindingIndex] + attributeOffsets[attribIndex];
			int stride = bindingStrides[bindingIndex];

			if (attribute instanceof VertexAttribute.Float f) {
				GL32.glVertexAttribPointer(attribIndex, f.size(), f.type()
						.getGlEnum(), f.normalized(), stride, offset);
			} else if (attribute instanceof VertexAttribute.Int vi) {
				GL32.glVertexAttribIPointer(attribIndex, vi.size(), vi.type()
						.getGlEnum(), stride, offset);
			}

			int divisor = bindingDivisors[bindingIndex];
			if (divisor != 0) {
				setDivisor(attribIndex, divisor);
			}
		}

		GlBufferType.ELEMENT_ARRAY_BUFFER.bind(elementBufferBinding);

		attributeDirty.clear();
	}

	protected abstract void setDivisor(int attribIndex, int divisor);

	@Override
	public void bindVertexBuffer(int bindingIndex, int vbo, long offset, int stride) {
		if (bindingBuffers[bindingIndex] != vbo || bindingOffsets[bindingIndex] != offset || bindingStrides[bindingIndex] != stride) {
			bindingBuffers[bindingIndex] = vbo;
			bindingOffsets[bindingIndex] = offset;
			bindingStrides[bindingIndex] = stride;

			for (int attribIndex = 0; attribIndex < attributeBindings.length; attribIndex++) {
				if (attributeBindings[attribIndex] == bindingIndex) {
					attributeDirty.set(attribIndex);
				}
			}
		}
	}

	@Override
	public void setBindingDivisor(int bindingIndex, int divisor) {
		if (bindingDivisors[bindingIndex] != divisor) {
			bindingDivisors[bindingIndex] = divisor;
		}
	}

	@Override
	public void bindAttributes(int bindingIndex, int startAttribIndex, List<VertexAttribute> vertexAttributes) {
		int attribIndex = startAttribIndex;
		int offset = 0;

		for (VertexAttribute attribute : vertexAttributes) {
			attributeBindings[attribIndex] = bindingIndex;
			attributes[attribIndex] = attribute;
			attributeOffsets[attribIndex] = offset;

			attributeDirty.set(attribIndex);

			attribIndex++;
			offset += attribute.getByteWidth();
		}
	}

	@Override
	public void setElementBuffer(int ebo) {
		elementBufferBinding = ebo;
	}

	public static class Core33 extends GlVertexArrayGL3 {
		public static final boolean SUPPORTED = isSupported();

		@Override
		protected void setDivisor(int attribIndex, int divisor) {
			GL33C.glVertexAttribDivisor(attribIndex, divisor);
		}

		private static boolean isSupported() {
			return Checks.checkFunctions(GlCompat.CAPABILITIES.glVertexAttribDivisor);
		}
	}

	public static class ARB extends GlVertexArrayGL3 {
		public static final boolean SUPPORTED = isSupported();

		@Override
		protected void setDivisor(int attribIndex, int divisor) {
			ARBInstancedArrays.glVertexAttribDivisorARB(attribIndex, divisor);
		}

		private static boolean isSupported() {
			return Checks.checkFunctions(GlCompat.CAPABILITIES.glVertexAttribDivisorARB);
		}
	}

	public static class Core extends GlVertexArrayGL3 {
		@Override
		protected void setDivisor(int attribIndex, int divisor) {
			throw new UnsupportedOperationException("Instanced arrays are not supported");
		}

		@Override
		public void setBindingDivisor(int bindingIndex, int divisor) {
			throw new UnsupportedOperationException("Instanced arrays are not supported");
		}
	}
}
