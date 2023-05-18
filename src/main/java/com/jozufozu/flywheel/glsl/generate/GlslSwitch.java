package com.jozufozu.flywheel.glsl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.util.Pair;
import com.jozufozu.flywheel.util.StringUtil;

public class GlslSwitch implements GlslStmt {

	private final GlslExpr on;

	private final List<Pair<GlslExpr, GlslBlock>> cases = new ArrayList<>();
	private GlslBlock defaultCase = null;

	private GlslSwitch(GlslExpr on) {
		this.on = on;
	}

	public static GlslSwitch on(GlslExpr on) {
		return new GlslSwitch(on);
	}

	public void intCase(int expr, GlslBlock block) {
		cases.add(Pair.of(GlslExpr.intLiteral(expr), block));
	}

	public void uintCase(int expr, GlslBlock block) {
		cases.add(Pair.of(GlslExpr.uintLiteral(expr), block));
	}

	public void defaultCase(GlslBlock block) {
		defaultCase = block;
	}

	@Override
	public String prettyPrint() {
		return """
				switch (%s) {
            %s
            }""".formatted(on.prettyPrint(), formatCases());
	}

    @NotNull
    private String formatCases() {
        var cases = this.cases.stream()
            .map(GlslSwitch::prettyPrintCase)
            .collect(Collectors.joining("\n"));
        if (defaultCase != null) {
            cases += "\ndefault:\n" + StringUtil.indent(defaultCase.prettyPrint(), 4);
        }
        return cases;
    }

	private static String prettyPrintCase(Pair<GlslExpr, GlslBlock> p) {
		var variant = p.first()
				.prettyPrint();
		var block = p.second()
				.prettyPrint();
		return """
				case %s:
				%s""".formatted(variant, StringUtil.indent(block, 4));
	}

}
