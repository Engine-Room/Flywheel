package com.jozufozu.flywheel.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

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
