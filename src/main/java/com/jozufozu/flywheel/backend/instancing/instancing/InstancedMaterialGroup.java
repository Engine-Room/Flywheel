package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.RenderLayer;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.model.FallbackAllocator;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.backend.model.ModelPool;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.compile.ProgramContext;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.Textures;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.RenderType;

/**
 * A group of materials all rendered with the same GL state.
 *
 * The children of a material group will all be rendered at the same time.
 * No guarantees are made about the order of draw calls.
 */
public class InstancedMaterialGroup<P extends WorldProgram> {

	protected final InstancingEngine<P> owner;
	protected final RenderType type;

	public InstancedMaterialGroup(InstancingEngine<P> owner, RenderType type) {
		this.owner = owner;
		this.type = type;
	}

	public void render(Matrix4f viewProjection, double camX, double camY, double camZ, RenderLayer layer) {
		type.setupRenderState();
		Textures.bindActiveTextures();
		//renderAll(viewProjection, camX, camY, camZ, layer);
		type.clearRenderState();
	}

	protected void setup(P program) {

	}
}
