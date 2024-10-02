package dev.engine_room.flywheel.backend.compile.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.backend.glsl.SourceFile;
import dev.engine_room.flywheel.backend.glsl.generate.FnSignature;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBlock;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBuilder;
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr;
import dev.engine_room.flywheel.backend.glsl.generate.GlslSwitch;
import net.minecraft.resources.ResourceLocation;

public class UberShaderComponent implements SourceComponent {
	private final ResourceLocation name;
	private final GlslExpr switchArg;
	private final List<AdaptedFn> functionsToAdapt;
	private final List<StringSubstitutionComponent> adaptedComponents;

	private UberShaderComponent(ResourceLocation name, GlslExpr switchArg, List<AdaptedFn> functionsToAdapt, List<StringSubstitutionComponent> adaptedComponents) {
		this.name = name;
		this.switchArg = switchArg;
		this.functionsToAdapt = functionsToAdapt;
		this.adaptedComponents = adaptedComponents;
	}

	public static Builder builder(ResourceLocation name) {
		return new Builder(name);
	}

	@Override
	public String name() {
		return Flywheel.rl("uber_shader").toString() + " / " + name;
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

			builder.blankLine();
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

			sw.uintCase(i, block);
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

	private record AdaptedFn(FnSignature signature, @Nullable GlslExpr defaultReturn) {
	}

	public static class Builder {
		private final ResourceLocation name;
		private final List<ResourceLocation> materialSources = new ArrayList<>();
		private final List<AdaptedFn> adaptedFunctions = new ArrayList<>();
		@Nullable
		private GlslExpr switchArg;

		public Builder(ResourceLocation name) {
			this.name = name;
		}

		public Builder materialSources(List<ResourceLocation> sources) {
			this.materialSources.addAll(sources);
			return this;
		}

		public Builder adapt(FnSignature function) {
			adaptedFunctions.add(new AdaptedFn(function, null));
			return this;
		}

		public Builder adapt(FnSignature function, GlslExpr defaultReturn) {
			adaptedFunctions.add(new AdaptedFn(function, defaultReturn));
			return this;
		}

		public Builder switchOn(GlslExpr expr) {
			this.switchArg = expr;
			return this;
		}

		public UberShaderComponent build(ShaderSources sources) {
			if (switchArg == null) {
				throw new NullPointerException("Switch argument must be set");
			}

			var transformed = ImmutableList.<StringSubstitutionComponent>builder();

			int index = 0;
			for (var rl : materialSources) {
				SourceFile sourceFile = sources.get(rl);
				final int finalIndex = index;
				var adapterMap = createAdapterMap(adaptedFunctions, fnName -> "_" + fnName + "_" + finalIndex);
				transformed.add(new StringSubstitutionComponent(sourceFile, adapterMap));
				index++;
			}

			return new UberShaderComponent(name, switchArg, adaptedFunctions, transformed.build());
		}

		private static ImmutableMap<String, String> createAdapterMap(List<AdaptedFn> adaptedFunctions, UnaryOperator<String> nameAdapter) {
			ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

			for (var adapted : adaptedFunctions) {
				var fnName = adapted.signature()
						.name();
				builder.put(fnName, nameAdapter.apply(fnName));
			}

			return builder.build();
		}
	}
}
