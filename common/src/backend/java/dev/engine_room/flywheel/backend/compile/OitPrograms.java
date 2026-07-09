package dev.engine_room.flywheel.backend.compile;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.core.CompilationHarness;
import dev.engine_room.flywheel.backend.compile.core.Compile;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.lib.util.ResourceUtil;
import net.minecraft.resources.Identifier;

public class OitPrograms {
	private static final Identifier FULLSCREEN = ResourceUtil.rl("internal/fullscreen.vert");
	static final Identifier OIT_COMPOSITE = ResourceUtil.rl("internal/oit_composite.frag");
	static final Identifier OIT_DEPTH = ResourceUtil.rl("internal/oit_depth.frag");

	private static final Compile<Identifier> COMPILE = new Compile<>();

	private final CompilationHarness<Identifier> harness;

	public OitPrograms(CompilationHarness<Identifier> harness) {
		this.harness = harness;
	}

	public static OitPrograms createFullscreenCompiler(ShaderSources sources) {
		var harness = COMPILE.program()
				.link(COMPILE.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.VERTEX)
						.nameMapper($ -> "fullscreen/fullscreen")
						.withResource(FULLSCREEN))
				.link(COMPILE.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.FRAGMENT)
						.nameMapper(rl -> "fullscreen/" + ResourceUtil.toDebugFileNameNoExtension(rl))
						.onCompile((rl, compilation) -> {
							if (GlCompat.MAX_GLSL_VERSION.compareTo(GlslVersion.V400) < 0) {
								// Need to define FMA for the wavelet calculations
								compilation.define("fma(a, b, c) ((a) * (b) + (c))");
							}
						})
						.withResource(s -> s))
				.postLink((key, program) -> {
					program.bind();
					Uniforms.setUniformBlockBindings(program);
					program.setSamplerBinding("_flw_accumulate", GlTextureUnit.T0);
					program.setSamplerBinding("_flw_depthRange", Samplers.DEPTH_RANGE);
					program.setSamplerBinding("_flw_coefficients", Samplers.COEFFICIENTS);

					GlProgram.unbind();
				})
				.harness("fullscreen", sources);
		return new OitPrograms(harness);
	}

	public GlProgram getOitCompositeProgram() {
		return harness.get(OitPrograms.OIT_COMPOSITE);
	}

	public GlProgram getOitDepthProgram() {
		return harness.get(OitPrograms.OIT_DEPTH);
	}

	public void delete() {
		harness.delete();
	}
}
