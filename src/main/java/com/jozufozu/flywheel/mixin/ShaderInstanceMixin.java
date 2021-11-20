package com.jozufozu.flywheel.mixin;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.util.TextureBinder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;

@Mixin(ShaderInstance.class)
public class ShaderInstanceMixin implements TextureBinder {
	@Shadow
	@Final
	private List<Integer> samplerLocations;

	@Shadow
	@Final
	private List<String> samplerNames;

	@Shadow
	@Final
	private Map<String, Object> samplerMap;

	@Override
	public void bind() {
		int i = GlStateManager._getActiveTexture();

		for(int j = 0; j < samplerLocations.size(); ++j) {
			String s = samplerNames.get(j);
			if (samplerMap.get(s) != null) {
				RenderSystem.activeTexture('\u84c0' + j);
				RenderSystem.enableTexture();
				Object object = this.samplerMap.get(s);
				int l = -1;
				if (object instanceof RenderTarget) {
					l = ((RenderTarget)object).getColorTextureId();
				} else if (object instanceof AbstractTexture) {
					l = ((AbstractTexture)object).getId();
				} else if (object instanceof Integer) {
					l = (Integer)object;
				}

				if (l != -1) {
					RenderSystem.bindTexture(l);
				}
			}
		}

		GlStateManager._activeTexture(i);
	}
}
