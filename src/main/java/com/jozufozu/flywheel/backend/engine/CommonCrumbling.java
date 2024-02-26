package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.api.material.WriteMask;
import com.jozufozu.flywheel.lib.material.CutoutShaders;
import com.jozufozu.flywheel.lib.material.FogShaders;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;

public class CommonCrumbling {
	public static void applyCrumblingProperties(SimpleMaterial.Builder crumblingMaterial, Material baseMaterial) {
		crumblingMaterial.copyFrom(baseMaterial)
				.fog(FogShaders.NONE)
				.cutout(CutoutShaders.ONE_TENTH)
				.polygonOffset(true)
				.transparency(Transparency.CRUMBLING)
				.writeMask(WriteMask.COLOR)
				.useOverlay(false)
				.useLight(false)
				.diffuse(false);
	}
}
