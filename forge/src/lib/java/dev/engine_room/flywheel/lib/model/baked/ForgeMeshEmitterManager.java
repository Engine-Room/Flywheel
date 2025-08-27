package dev.engine_room.flywheel.lib.model.baked;

import net.minecraft.client.renderer.RenderType;

class ForgeMeshEmitterManager extends MeshEmitterManager<ForgeMeshEmitter> {
	ForgeMeshEmitterManager() {
		super(ForgeMeshEmitter::new);
	}

	public ForgeMeshEmitter getEmitter(RenderType renderType) {
		return emitterMap.get(renderType);
	}
}
