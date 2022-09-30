package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.Collection;
import java.util.List;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.generate.GlslBuilder;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class VertexMaterialComponent implements SourceComponent {

	private static final String flw_materialVertex = "flw_materialVertex";
	private static final GlslExpr flw_materialVertexID = GlslExpr.variable(flw_materialVertex + "ID");

	private final List<TransformedSourceComponent> transformedMaterials;

	public VertexMaterialComponent(List<FileResolution> sourceMaterials) {

		this.transformedMaterials = sourceMaterials.stream()
				.map(FileResolution::getFile)
				.map(s -> {
					var newName = flw_materialVertex + '_' + ResourceUtil.toSafeString(s.name());
					return new TransformedSourceComponent(s, flw_materialVertex, newName);
				})
				.toList();
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return transformedMaterials;
	}

	@Override
	public String source(CompilationContext ctx) {
		String out = genSource();
		return ctx.generatedHeader(out, "material adapter") + out;
	}

	public String genSource() {
		var builder = new GlslBuilder();

		builder.function()
				.returnType("void")
				.name("flw_materialVertex")
				.body(this::accept);

		return builder.build();
	}

	@Override
	public ResourceLocation name() {
		return Flywheel.rl("vertex_material_adapter");
	}

	private void accept(GlslBuilder.BlockBuilder body) {
		var sw = new GlslBuilder.SwitchBuilder(flw_materialVertexID);
		for (int i = 0; i < transformedMaterials.size(); i++) {
			var variant = transformedMaterials.get(i).replacement;

			sw.case_(i, b -> b.eval(GlslExpr.call(variant))
					.break_());
		}
		body.add(sw.build());
	}
}
