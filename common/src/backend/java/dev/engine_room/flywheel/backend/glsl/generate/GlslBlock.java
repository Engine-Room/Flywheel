package dev.engine_room.flywheel.backend.glsl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GlslBlock {
	private final List<GlslStmt> body = new ArrayList<>();

	public static GlslBlock create() {
		return new GlslBlock();
	}

	public GlslBlock add(GlslStmt stmt) {
		body.add(stmt);
		return this;
	}

	public GlslBlock eval(GlslExpr expr) {
		return add(GlslStmt.eval(expr));
	}

	public GlslBlock ret(GlslExpr call) {
		add(GlslStmt.ret(call));
		return this;
	}

	public GlslBlock breakStmt() {
		add(GlslStmt.BREAK);
		return this;
	}

	public String prettyPrint() {
		return body.stream()
				.map(GlslStmt::prettyPrint)
				.collect(Collectors.joining("\n"));
	}

	public void raw(String s) {
		add(GlslStmt.raw(s));
	}
}
