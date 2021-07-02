package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;
import com.mojang.serialization.Codec;

import net.minecraft.util.ResourceLocation;

public class BooleanGameStateCondition implements IGameStateCondition {

	public static final Codec<BooleanGameStateCondition> BOOLEAN_SUGAR = IGameStateProvider.CODEC.xmap(gameContext -> {
		if (gameContext instanceof IBooleanStateProvider) {
			return new BooleanGameStateCondition(((IBooleanStateProvider) gameContext));
		}

		return null;
	}, IGameStateCondition::getStateProvider);
	protected final IBooleanStateProvider context;

	public BooleanGameStateCondition(IBooleanStateProvider context) {
		this.context = context;
	}

	@Override
	public ResourceLocation getID() {
		return context.getID();
	}

	@Override
	public IGameStateProvider getStateProvider() {
		return context;
	}

	@Override
	public boolean isMet() {
		return context.isTrue();
	}
}
