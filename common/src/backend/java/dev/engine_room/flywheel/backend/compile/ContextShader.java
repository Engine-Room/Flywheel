package dev.engine_room.flywheel.backend.compile;

import java.util.Locale;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.core.Compilation;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;

public enum ContextShader {
	DEFAULT(null, $ -> {
	}),
	CRUMBLING("_FLW_CRUMBLING", program -> program.setSamplerBinding("_flw_crumblingTex", Samplers.CRUMBLING)),
	EMBEDDED("FLW_EMBEDDED", $ -> {
	});

	@Nullable
	private final String define;
	private final Consumer<GlProgram> onLink;

	ContextShader(@Nullable String define, Consumer<GlProgram> onLink) {
		this.define = define;
		this.onLink = onLink;
	}

	public void onLink(GlProgram program) {
		onLink.accept(program);
	}

	public void onCompile(Compilation comp) {
		if (define != null) {
			comp.define(define);
		}
	}

	public String nameLowerCase() {
		return name().toLowerCase(Locale.ROOT);
	}
}
