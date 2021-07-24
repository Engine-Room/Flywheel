package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.backend.state.NoCullRenderState;
import com.jozufozu.flywheel.backend.state.RenderState;

import net.minecraft.client.renderer.Atlases;

public class RenderStates {
	public static final IRenderState SHULKER = RenderState.builder()
			.texture(Atlases.SHULKER_SHEET)
			.addState(NoCullRenderState.INSTANCE)
			.build();
}
