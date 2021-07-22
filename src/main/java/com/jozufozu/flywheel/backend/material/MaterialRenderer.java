package com.jozufozu.flywheel.backend.material;

import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.core.shader.IProgramCallback;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.util.math.vector.Matrix4f;

public class MaterialRenderer<P extends WorldProgram> {

	protected final Supplier<P> program;
	protected final InstanceMaterial<?> material;

	public MaterialRenderer(Supplier<P> programSupplier, InstanceMaterial<?> material) {
		this.program = programSupplier;
		this.material = material;
	}

	public void render(Matrix4f viewProjection, double camX, double camY, double camZ, IProgramCallback<P> setup) {
		if (material.nothingToRender()) return;

		P program = this.program.get();

		program.bind();
		program.uploadViewProjection(viewProjection);
		program.uploadCameraPos(camX, camY, camZ);

		if (setup != null) setup.call(program);

		material.forEachInstancer(Instancer::render);
	}

}
