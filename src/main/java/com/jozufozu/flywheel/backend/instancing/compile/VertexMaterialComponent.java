package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.List;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderSources;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;

import net.minecraft.resources.ResourceLocation;

public class VertexMaterialComponent extends MaterialAdapterComponent {

	private static final String flw_materialVertex = "flw_materialVertex";
	private static final List<String> adaptedFunctions = List.of(flw_materialVertex);
	private static final GlslExpr flw_materialVertexID = GlslExpr.variable(flw_materialVertex + "ID");


	public VertexMaterialComponent(ShaderSources sources, List<FileResolution> sourceMaterials) {
		super(sources, sourceMaterials, flw_materialVertexID, adaptedFunctions);
	}

	@Override
	public ResourceLocation name() {
		return Flywheel.rl("vertex_material_adapter");
	}
}
