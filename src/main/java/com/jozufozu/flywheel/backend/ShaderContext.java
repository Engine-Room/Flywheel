package com.jozufozu.flywheel.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.IMultiProgram;

import net.minecraft.util.ResourceLocation;

public abstract class ShaderContext<P extends GlProgram> implements IShaderContext<P> {

	protected final Map<ResourceLocation, IMultiProgram<P>> programs = new HashMap<>();

	public final Backend backend;

	public ShaderContext(Backend backend) {
		this.backend = backend;
	}

	@Override
	public Supplier<P> getProgramSupplier(ResourceLocation spec) {
		return programs.get(spec);
	}

	@Override
	public void delete() {
		programs.values()
				.forEach(IMultiProgram::delete);
		programs.clear();
	}
}
