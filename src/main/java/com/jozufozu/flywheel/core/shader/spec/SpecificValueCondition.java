package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.GameStateProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public class SpecificValueCondition implements GameStateCondition {

	public static final Codec<SpecificValueCondition> CODEC = RecordCodecBuilder.create(condition -> condition.group(GameStateProvider.CODEC.fieldOf("provider")
					.forGetter(SpecificValueCondition::getStateProvider), Codec.STRING.fieldOf("value")
					.forGetter(SpecificValueCondition::getValue))
			.apply(condition, SpecificValueCondition::new));

	private final String required;
	private final GameStateProvider context;

	public SpecificValueCondition(GameStateProvider context, String required) {
		this.required = required;
		this.context = context;
	}

	@Override
	public ResourceLocation getID() {
		return context.getID();
	}

	public String getValue() {
		return required;
	}

	@Override
	public GameStateProvider getStateProvider() {
		return context;
	}

	@Override
	public boolean isMet() {
		return required.equals(context.getValue()
									   .toString());
	}
}
