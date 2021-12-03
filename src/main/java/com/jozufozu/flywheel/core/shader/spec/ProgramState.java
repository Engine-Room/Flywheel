package com.jozufozu.flywheel.core.shader.spec;

import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.util.CodecUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ProgramState(IGameStateCondition context, List<String> defines) {

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
					.forGetter(ProgramState::context), CodecUtil.oneOrMore(Codec.STRING)
					.optionalFieldOf("define", Collections.emptyList())
					.forGetter(ProgramState::defines))
			.apply(state, ProgramState::new));
}
