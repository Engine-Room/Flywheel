package dev.engine_room.flywheel.backend.glsl.generate;

import java.util.function.Consumer;

import dev.engine_room.flywheel.lib.util.StringUtil;

public class GlslFn implements GlslBuilder.Declaration {
	private FnSignature signature;
	private GlslBlock body = new GlslBlock();

	public GlslFn signature(FnSignature signature) {
		this.signature = signature;
		return this;
	}

	public GlslFn body(GlslBlock block) {
		body = block;
		return this;
	}

	public GlslFn body(Consumer<GlslBlock> f) {
		f.accept(body);
		return this;
	}

	@Override
	public String prettyPrint() {
		return """
				%s {
				%s
				}""".formatted(signature.fullDeclaration(), StringUtil.indent(body.prettyPrint(), 4));
	}
}
