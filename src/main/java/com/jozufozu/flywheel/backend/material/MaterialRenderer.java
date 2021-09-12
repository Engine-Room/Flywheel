package com.jozufozu.flywheel.backend.material;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.instancing.GPUInstancer;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.util.math.vector.Matrix4f;

public class MaterialRenderer<P extends WorldProgram> {

	protected final Supplier<P> program;
	protected final MaterialImpl<?> material;

	protected final Consumer<P> setupFunc;

	public MaterialRenderer(Supplier<P> programSupplier, MaterialImpl<?> material, Consumer<P> setupFunc) {
		this.program = programSupplier;
		this.material = material;
		this.setupFunc = setupFunc;
	}

	public void render(Matrix4f viewProjection, double camX, double camY, double camZ) {
		if (material.nothingToRender()) return;

		Collection<? extends GPUInstancer<?>> instancers = material.models.asMap()
				.values();

		// initialize all uninitialized instancers...
		instancers.forEach(GPUInstancer::init);
		// ...and then flush the model arena in case anything was marked for upload
		material.modelPool.flush();

		P program = this.program.get();

		program.bind();
		program.uploadViewProjection(viewProjection);
		program.uploadCameraPos(camX, camY, camZ);

		setupFunc.accept(program);

		instancers.forEach(GPUInstancer::render);
	}

}
