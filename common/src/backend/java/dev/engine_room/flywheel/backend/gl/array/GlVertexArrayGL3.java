package dev.engine_room.flywheel.backend.gl.array;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.Checks;

import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.buffer.GlBufferType;
import net.minecraft.Util;

public abstract class GlVertexArrayGL3 extends GlVertexArray {
	private final BitSet attributeDirty = new BitSet(MAX_ATTRIBS);
	private final int[] attributeOffsets = new int[MAX_ATTRIBS];
	private final VertexAttribute[] attributes = new VertexAttribute[MAX_ATTRIBS];
	private final int[] attributeBindings = Util.make(new int[MAX_ATTRIBS], a -> Arrays.fill(a, -1));
	private final int[] bindingBuffers = new int[MAX_ATTRIB_BINDINGS];
	private final long[] bindingOffsets = new long[MAX_ATTRIB_BINDINGS];
	private final int[] bindingStrides = new int[MAX_ATTRIB_BINDINGS];
	private final int[] bindingDivisors = new int[MAX_ATTRIB_BINDINGS];
	private int requestedElementBuffer = 0;
	private int boundElementBuffer = 0;

	public GlVertexArrayGL3() {
		handle(GL30.glGenVertexArrays());
	}

	@Override
	public void bindForDraw() {
		super.bindForDraw();

		maybeUpdateAttributes();

		maybeUpdateEBOBinding();
	}

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
			offset += attribute.byteWidth();
		}
	}

	@Override
	public void setElementBuffer(int ebo) {
		requestedElementBuffer = ebo;
	}

	private void maybeUpdateEBOBinding() {
		if (requestedElementBuffer != boundElementBuffer) {
			GlBufferType.ELEMENT_ARRAY_BUFFER.bind(requestedElementBuffer);
			boundElementBuffer = requestedElementBuffer;
		}
	}

	private void maybeUpdateAttributes() {
		for (int attribIndex = attributeDirty.nextSetBit(0); attribIndex < MAX_ATTRIB_BINDINGS && attribIndex >= 0; attribIndex = attributeDirty.nextSetBit(attribIndex + 1)) {
			updateAttribute(attribIndex);
		}
		attributeDirty.clear();
	}

	private void updateAttribute(int attribIndex) {
		int bindingIndex = attributeBindings[attribIndex];
		var attribute = attributes[attribIndex];

		if (bindingIndex == -1 || attribute == null) {
			return;
		}

		GlBufferType.ARRAY_BUFFER.bind(bindingBuffers[bindingIndex]);
		GL20C.glEnableVertexAttribArray(attribIndex);

		long offset = bindingOffsets[bindingIndex] + attributeOffsets[attribIndex];
		int stride = bindingStrides[bindingIndex];

		if (attribute instanceof VertexAttribute.Float f) {
			GL32.glVertexAttribPointer(attribIndex, f.size(), f.type()
					.glEnum(), f.normalized(), stride, offset);
		} else if (attribute instanceof VertexAttribute.Int vi) {
			GL32.glVertexAttribIPointer(attribIndex, vi.size(), vi.type()
					.glEnum(), stride, offset);
		}

		int divisor = bindingDivisors[bindingIndex];
		if (divisor != 0) {
			setDivisor(attribIndex, divisor);
		}
	}

	protected abstract void setDivisor(int attribIndex, int divisor);

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
