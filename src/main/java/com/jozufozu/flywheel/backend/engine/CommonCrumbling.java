package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.api.material.WriteMask;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.jozufozu.flywheel.lib.material.CutoutShaders;
import com.jozufozu.flywheel.lib.material.FogShaders;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

public class CommonCrumbling {
	public static void applyCrumblingProperties(SimpleMaterial.Builder crumblingMaterial, Material baseMaterial) {
		crumblingMaterial.copyFrom(baseMaterial)
				.fog(FogShaders.NONE)
				.cutout(CutoutShaders.ONE_TENTH)
				.polygonOffset(true)
				.transparency(Transparency.CRUMBLING)
				.writeMask(WriteMask.COLOR)
				.useOverlay(false)
				.useLight(false);
	}

	public static int getDiffuseTexture(Material material) {
		return Minecraft.getInstance()
				.getTextureManager()
				.getTexture(material.texture())
				.getId();
	}

	public static void setActiveAndBindForCrumbling(int diffuseTexture) {
		GlTextureUnit.T1.makeActive();
		RenderSystem.setShaderTexture(1, diffuseTexture);
		RenderSystem.bindTexture(diffuseTexture);
	}
}
