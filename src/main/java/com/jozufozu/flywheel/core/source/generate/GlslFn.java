package com.jozufozu.flywheel.core.source.generate;

import java.util.function.Consumer;

import com.jozufozu.flywheel.util.StringUtil;

public class GlslFn implements GlslBuilder.Declaration {
	private final GlslBlock body = new GlslBlock();
	private FnSignature signature;

	public GlslFn signature(FnSignature signature) {
		this.signature = signature;
		return this;
	}

	public GlslFn body(Consumer<GlslBlock> f) {
		f.accept(body);
		return this;
	}

	public String prettyPrint() {
		return """
				%s {
				%s
				}
				""".formatted(signature.fullDeclaration(), StringUtil.indent(body.prettyPrint(), 4));
	}
}
