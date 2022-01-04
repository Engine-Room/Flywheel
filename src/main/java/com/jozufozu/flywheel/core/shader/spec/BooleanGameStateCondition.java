package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.GameStateProvider;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;

public class BooleanGameStateCondition implements GameStateCondition {

	public static final Codec<BooleanGameStateCondition> BOOLEAN_SUGAR = GameStateProvider.CODEC.xmap(gameContext -> {
		if (gameContext instanceof BooleanStateProvider) {
			return new BooleanGameStateCondition(((BooleanStateProvider) gameContext));
		}

		return null;
	}, GameStateCondition::getStateProvider);
	protected final BooleanStateProvider context;

	public BooleanGameStateCondition(BooleanStateProvider context) {
		this.context = context;
	}

	@Override
	public ResourceLocation getID() {
		return context.getID();
	}

	@Override
	public GameStateProvider getStateProvider() {
		return context;
	}

	@Override
	public boolean isMet() {
		return context.isTrue();
	}
}
