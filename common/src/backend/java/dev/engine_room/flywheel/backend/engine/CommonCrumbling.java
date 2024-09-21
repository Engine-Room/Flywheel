package dev.engine_room.flywheel.backend.engine;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.FogShaders;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;

public class CommonCrumbling {
	public static void applyCrumblingProperties(SimpleMaterial.Builder crumblingMaterial, Material baseMaterial) {
		crumblingMaterial.copyFrom(baseMaterial)
				.fog(FogShaders.NONE)
				.cutout(CutoutShaders.ONE_TENTH)
				.light(LightShaders.SMOOTH_WHEN_EMBEDDED)
				.polygonOffset(true)
				.transparency(Transparency.CRUMBLING)
				.writeMask(WriteMask.COLOR)
				.useOverlay(false)
				.useLight(false)
				.diffuse(false);
	}
}
