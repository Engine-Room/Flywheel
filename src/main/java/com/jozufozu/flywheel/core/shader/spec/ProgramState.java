package com.jozufozu.flywheel.core.shader.spec;

import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;
import com.jozufozu.flywheel.util.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ProgramState(IGameStateProvider context, List<String> defines) {

	public static final Codec<ProgramState> CODEC = RecordCodecBuilder.create(state -> state.group(IGameStateProvider.CODEC.fieldOf("when")
					.forGetter(ProgramState::context), CodecUtil.oneOrMore(Codec.STRING)
					.optionalFieldOf("define", Collections.emptyList())
					.forGetter(ProgramState::defines))
			.apply(state, ProgramState::new));
}
