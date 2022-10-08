package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderSources;
import com.jozufozu.flywheel.core.source.generate.GlslBuilder;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public abstract class MaterialAdapterComponent implements SourceComponent {

	// TODO: material id handling in pipeline shader
	private final GlslExpr switchArg;
	private final List<String> adaptedFunctions;
	private final List<RenamedFunctionsSourceComponent> transformedMaterials;

	// TODO: Create builder and remove Fragment* and Vertex* classes
	public MaterialAdapterComponent(ShaderSources sources, List<FileResolution> sourceMaterials, GlslExpr switchArg, List<String> adaptedFunctions) {
		this.switchArg = switchArg;
		this.adaptedFunctions = adaptedFunctions;

		var transformed = ImmutableList.<RenamedFunctionsSourceComponent>builder();

		for (FileResolution fileResolution : sourceMaterials) {
			var loc = fileResolution.resourceLocation();
			var sourceFile = sources.find(loc);

			transformed.add(new RenamedFunctionsSourceComponent(sourceFile, createAdapterMap(adaptedFunctions, loc)));
		}

		this.transformedMaterials = transformed.build();
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return transformedMaterials;
	}

	@Override
	public String source() {
		var builder = new GlslBuilder();

		for (String adaptedFunction : adaptedFunctions) {
			// TODO: support different function signatures
			builder.function()
					.returnType("void")
					.name(adaptedFunction)
					.body(body -> generateAdapter(body, adaptedFunction));
		}

		return builder.build();
	}

	private void generateAdapter(GlslBuilder.BlockBuilder body, String adaptedFunction) {
		var sw = new GlslBuilder.SwitchBuilder(switchArg);
		for (int i = 0; i < transformedMaterials.size(); i++) {
			var variant = transformedMaterials.get(i)
					.replacement(adaptedFunction);

			sw.case_(i, b -> b.eval(GlslExpr.call(variant))
					.break_());
		}
		body.add(sw.build());
	}

	@NotNull
	private static HashMap<String, String> createAdapterMap(List<String> adaptedFunctions, ResourceLocation loc) {
		HashMap<String, String> out = new HashMap<>();

		var suffix = '_' + ResourceUtil.toSafeString(loc);

		for (String fnName : adaptedFunctions) {
			out.put(fnName, fnName + suffix);
		}

		return out;
	}
}
