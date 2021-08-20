package com.jozufozu.flywheel.backend.gl.versioned.instancing;

import org.lwjgl.opengl.ARBDrawElementsBaseVertex;
import org.lwjgl.opengl.ARBDrawInstanced;
import org.lwjgl.opengl.EXTDrawInstanced;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.versioned.GlVersioned;

public enum BaseVertex implements GlVersioned {
	GL31_CORE {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL31;
		}

		@Override
		public void drawElementsInstancedBaseVertex(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int instanceCount, int baseVertex) {
			GL32.glDrawElementsInstancedBaseVertex(mode.glEnum, elementCount, type.getGlEnum(), indices, instanceCount, baseVertex);
		}

		@Override
		public void drawElementsBaseVertex(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int baseVertex) {
			GL32.glDrawElementsBaseVertex(mode.glEnum, elementCount, type.getGlEnum(), indices, baseVertex);
		}
	},
	ARB {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_ARB_draw_elements_base_vertex;
		}

		@Override
		public void drawElementsInstancedBaseVertex(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int instanceCount, int baseVertex) {
			ARBDrawElementsBaseVertex.glDrawElementsInstancedBaseVertex(mode.glEnum, elementCount, type.getGlEnum(), indices, instanceCount, baseVertex);
		}

		@Override
		public void drawElementsBaseVertex(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int baseVertex) {
			ARBDrawElementsBaseVertex.glDrawElementsBaseVertex(mode.glEnum, elementCount, type.getGlEnum(), indices, baseVertex);
		}
	},
	UNSUPPORTED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return true;
		}
	};


	public void drawElementsInstancedBaseVertex(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int instanceCount, int baseVertex) {
		throw new UnsupportedOperationException();
	}


	public void drawElementsBaseVertex(GlPrimitive mode, int elementCount, GlNumericType type, long indices, int baseVertex) {
		throw new UnsupportedOperationException();
	}
}
