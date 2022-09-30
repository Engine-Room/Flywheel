package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.Collection;
import java.util.List;

import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class FragmentMaterialComponent implements SourceComponent {

	private static final String flw_materialFragment = "flw_materialFragment";
	private static final String flw_discardPredicate = "flw_discardPredicate";
	private static final String flw_fogFilter = "flw_fogFilter";

	private static final GlslExpr flw_materialFragmentID = GlslExpr.variable(flw_materialFragment + "ID");

	private final List<TransformedSourceComponent> transformedMaterials;

	public FragmentMaterialComponent(List<FileResolution> sourceMaterials) {

		this.transformedMaterials = sourceMaterials.stream()
				.map(FileResolution::getFile)
				.map(s -> {
					var newName = flw_materialFragment + '_' + ResourceUtil.toSafeString(s.name());
					return new TransformedSourceComponent(s, flw_materialFragment, newName);
				})
				.toList();
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return transformedMaterials;
	}

	@Override
	public String source(CompilationContext ctx) {
		return null;
	}

	@Override
	public ResourceLocation name() {
		return null;
	}
}
