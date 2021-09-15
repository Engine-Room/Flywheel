package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public class SpecificValueCondition implements IGameStateCondition {

	public static final Codec<SpecificValueCondition> CODEC = RecordCodecBuilder.create(condition -> condition.group(IGameStateProvider.CODEC.fieldOf("provider")
																															 .forGetter(SpecificValueCondition::getStateProvider), Codec.STRING.fieldOf("value")
																															 .forGetter(SpecificValueCondition::getValue))
			.apply(condition, SpecificValueCondition::new));

	private final String required;
	private final IGameStateProvider context;

	public SpecificValueCondition(IGameStateProvider context, String required) {
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
	public IGameStateProvider getStateProvider() {
		return context;
	}

	@Override
	public boolean isMet() {
		return required.equals(context.getValue()
									   .toString());
	}
}
