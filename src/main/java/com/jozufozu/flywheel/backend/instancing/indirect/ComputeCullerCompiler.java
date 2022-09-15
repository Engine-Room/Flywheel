package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.compile.CompileUtil;
import com.jozufozu.flywheel.core.compile.Memoizer;
import com.jozufozu.flywheel.core.compile.ProgramAssembler;
import com.jozufozu.flywheel.core.compile.ShaderCompilationException;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

import net.minecraft.resources.ResourceLocation;

public class ComputeCullerCompiler extends Memoizer<StructType<?>, GlProgram> {

	public static final ComputeCullerCompiler INSTANCE = new ComputeCullerCompiler();

	private ComputeCullerCompiler() {
	}

	@Override
	protected GlProgram _create(StructType<?> structType) {
		var location = structType.getInstanceShader();

		var finalSource = new StringBuilder();
		CompilationContext context = new CompilationContext();
		var components = List.of(new IndirectComponent(structType.getLayout().layoutItems), location.getFile(), Components.Pipeline.INDIRECT_CULL.getFile());

		var names = ImmutableList.<ResourceLocation>builder();
		var included = new LinkedHashSet<SourceComponent>(); // linked to preserve order
		for (var component : components) {
			included.addAll(component.included());
			names.add(component.name());
		}

		finalSource.append(CompileUtil.generateHeader(GLSLVersion.V460, ShaderType.COMPUTE));
		for (var include : included) {
			finalSource.append(include.source(context));
		}

		for (var component : components) {
			finalSource.append(component.source(context));
		}

		try {
			var fileLoc = location.getFileLoc();
			var shader = new GlShader(finalSource.toString(), ShaderType.COMPUTE, ImmutableList.of(fileLoc));

			return new ProgramAssembler(fileLoc).attachShader(shader)
					.link()
					.build(GlProgram::new);
		} catch (ShaderCompilationException e) {
			throw e.withErrorLog(context);
		}
	}

	@Override
	protected void _destroy(GlProgram value) {
		value.delete();
	}

	public static void invalidateAll(ReloadRenderersEvent ignored) {
		INSTANCE.invalidate();
	}
}
