package com.jozufozu.flywheel.core.shader.spec;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.core.shader.extension.IProgramExtension;
import com.jozufozu.flywheel.util.CodecUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ProgramState {

	// TODO: Use Codec.dispatch
	private static final Codec<IGameStateCondition> WHEN = Codec.either(BooleanGameStateCondition.BOOLEAN_SUGAR, SpecificValueCondition.CODEC)
			.flatXmap(either -> either.map(DataResult::success, DataResult::success), any -> {
				if (any instanceof BooleanGameStateCondition) {
					return DataResult.success(Either.left((BooleanGameStateCondition) any));
				}

				if (any instanceof SpecificValueCondition) {
					return DataResult.success(Either.right((SpecificValueCondition) any));
				}

				return DataResult.error("unknown context condition");
			});

	public static final Codec<ProgramState> CODEC = RecordCodecBuilder.create(state -> state.group(WHEN.fieldOf("when")
			.forGetter(ProgramState::getContext), CodecUtil.oneOrMore(Codec.STRING)
			.optionalFieldOf("define", Collections.emptyList())
			.forGetter(ProgramState::getDefines), CodecUtil.oneOrMore(IProgramExtension.CODEC)
			.optionalFieldOf("extend", Collections.emptyList())
			.forGetter(ProgramState::getExtensions))
			.apply(state, ProgramState::new));

	private final IGameStateCondition context;
	private final List<String> defines;
	private final List<IProgramExtension> extensions;

	public ProgramState(IGameStateCondition context, List<String> defines, List<IProgramExtension> extensions) {
		this.context = context;
		this.defines = defines;
		this.extensions = extensions;
	}

	public IGameStateCondition getContext() {
		return context;
	}

	public List<String> getDefines() {
		return defines;
	}

	public List<IProgramExtension> getExtensions() {
		return extensions;
	}

	@Override
	public String toString() {
		return "ProgramState{" + "gameState=" + context.getID() + ", defines=" + defines + ", extensions=" + extensions.stream().map(IProgramExtension::getID).collect(Collectors.toList()) + '}';
	}
}
