package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.List;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderSources;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;

import net.minecraft.resources.ResourceLocation;

public class FragmentMaterialComponent extends MaterialAdapterComponent {

	private static final String flw_materialFragment = "flw_materialFragment";
	private static final String flw_discardPredicate = "flw_discardPredicate";
	private static final String flw_fogFilter = "flw_fogFilter";
	private static final List<String> adaptedFunctions = List.of(flw_materialFragment, flw_discardPredicate, flw_fogFilter);

	private static final GlslExpr flw_materialFragmentID = GlslExpr.variable(flw_materialFragment + "ID");

	public FragmentMaterialComponent(ShaderSources sources, List<FileResolution> sourceMaterials) {
		super(sources, sourceMaterials, flw_materialFragmentID, adaptedFunctions);
	}

	@Override
	public ResourceLocation name() {
		return Flywheel.rl("fragment_material_adapter");
	}
}
