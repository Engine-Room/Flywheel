package com.jozufozu.flywheel.vanilla;

import java.util.List;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.backend.state.NoCullRenderState;
import com.jozufozu.flywheel.backend.state.RenderState;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.RenderMaterial;

public class RenderStates {
	public static final IRenderState SHULKER = RenderState.builder()
			.texture(Atlases.SHULKER_SHEET)
			.addState(NoCullRenderState.INSTANCE)
			.build();
}
