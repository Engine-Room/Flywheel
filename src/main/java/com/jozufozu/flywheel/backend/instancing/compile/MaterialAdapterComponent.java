package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderSources;
import com.jozufozu.flywheel.core.source.generate.FnSignature;
import com.jozufozu.flywheel.core.source.generate.GlslBlock;
import com.jozufozu.flywheel.core.source.generate.GlslBuilder;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;
import com.jozufozu.flywheel.core.source.generate.GlslSwitch;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class MaterialAdapterComponent implements SourceComponent {

	// TODO: material id handling in pipeline shader
	private final ResourceLocation name;
	private final GlslExpr switchArg;
	private final List<AdaptedFn> functionsToAdapt;
	private final List<RenamedFunctionsSourceComponent> adaptedComponents;

	public MaterialAdapterComponent(ResourceLocation name, GlslExpr switchArg, List<AdaptedFn> functionsToAdapt, List<RenamedFunctionsSourceComponent> adaptedComponents) {
		this.name = name;
		this.switchArg = switchArg;
		this.functionsToAdapt = functionsToAdapt;
		this.adaptedComponents = adaptedComponents;
	}

	public static Builder builder(ResourceLocation name) {
		return new Builder(name);
	}

	@Override
	public ResourceLocation name() {
		return name;
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return adaptedComponents;
	}

	@Override
	public String source() {
		var builder = new GlslBuilder();

		for (var adaptedFunction : functionsToAdapt) {
			builder.function()
					.signature(adaptedFunction.signature())
					.body(body -> generateAdapter(body, adaptedFunction));
		}

		return builder.build();
	}

	private void generateAdapter(GlslBlock body, AdaptedFn adaptedFunction) {
		var sw = GlslSwitch.on(switchArg);
		var fnSignature = adaptedFunction.signature();
		var fnName = fnSignature.name();
		var isVoid = fnSignature.isVoid();
		var fnArgs = fnSignature.createArgExpressions();

		for (int i = 0; i < adaptedComponents.size(); i++) {
			var component = adaptedComponents.get(i);

			if (!component.replaces(fnName)) {
				continue;
			}

			var adaptedCall = GlslExpr.call(component.remapFnName(fnName), fnArgs);

			var block = GlslBlock.create();
			if (isVoid) {
				block.eval(adaptedCall)
						.breakStmt();
			} else {
				block.ret(adaptedCall);
			}

			sw.intCase(i, block);
		}

		if (!isVoid) {
			var defaultReturn = adaptedFunction.defaultReturn;
			if (defaultReturn == null) {
				throw new IllegalStateException("Function " + fnName + " is not void, but no default return value was provided");
			}
			sw.defaultCase(GlslBlock.create()
					.ret(defaultReturn));
		}

		body.add(sw);
	}

	@NotNull
	private static HashMap<String, String> createAdapterMap(List<AdaptedFn> adaptedFunctions, ResourceLocation loc) {
		HashMap<String, String> out = new HashMap<>();

		var suffix = '_' + ResourceUtil.toSafeString(loc);

		for (var adapted : adaptedFunctions) {
			var fnName = adapted.signature()
					.name();
			out.put(fnName, fnName + suffix);
		}

		return out;
	}

	private record AdaptedFn(FnSignature signature, @Nullable GlslExpr defaultReturn) {
	}

	public static class Builder {
		private final ResourceLocation name;
		private final List<FileResolution> sourceMaterials = new ArrayList<>();
		private final List<AdaptedFn> adaptedFunctions = new ArrayList<>();
		private GlslExpr switchArg;

		public Builder(ResourceLocation name) {
			this.name = name;
		}

		public Builder materialSources(List<FileResolution> sources) {
			this.sourceMaterials.addAll(sources);
			return this;
		}

		public Builder adapt(FnSignature function) {
			adaptedFunctions.add(new AdaptedFn(function, null));
			return this;
		}

		public Builder adapt(FnSignature function, @Nonnull GlslExpr defaultReturn) {
			adaptedFunctions.add(new AdaptedFn(function, defaultReturn));
			return this;
		}

		public Builder switchOn(GlslExpr expr) {
			this.switchArg = expr;
			return this;
		}

		public MaterialAdapterComponent build(ShaderSources sources) {
			if (switchArg == null) {
				throw new NullPointerException("Switch argument must be set");
			}

			var transformed = ImmutableList.<RenamedFunctionsSourceComponent>builder();

			for (FileResolution fileResolution : sourceMaterials) {
				var loc = fileResolution.resourceLocation();
				var sourceFile = sources.find(loc);

				transformed.add(new RenamedFunctionsSourceComponent(sourceFile, createAdapterMap(adaptedFunctions, loc)));
			}

			return new MaterialAdapterComponent(name, switchArg, adaptedFunctions, transformed.build());
		}
	}
}
