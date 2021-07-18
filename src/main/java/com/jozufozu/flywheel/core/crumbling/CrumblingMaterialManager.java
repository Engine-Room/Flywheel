package com.jozufozu.flywheel.core.crumbling;

import java.util.ArrayList;
import java.util.Map;

import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.MaterialRenderer;
import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.core.atlas.AtlasInfo;
import com.jozufozu.flywheel.core.atlas.SheetData;
import com.jozufozu.flywheel.core.shader.IProgramCallback;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class CrumblingMaterialManager extends MaterialManager<CrumblingProgram> {

	public CrumblingMaterialManager(WorldContext<CrumblingProgram> context) {
		super(context);
	}

	/**
	 * Render every model for every material.
	 *
	 * @param layer          Which vanilla {@link RenderType} is being drawn?
	 * @param viewProjection How do we get from camera space to clip space?
	 * @param callback       Provide additional uniforms or state here.
	 */
	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, IProgramCallback<CrumblingProgram> callback) {
		camX -= originCoordinate.getX();
		camY -= originCoordinate.getY();
		camZ -= originCoordinate.getZ();

		Matrix4f translate = Matrix4f.createTranslateMatrix((float) -camX, (float) -camY, (float) -camZ);

		translate.multiplyBackward(viewProjection);

		for (Map.Entry<IRenderState, ArrayList<MaterialRenderer<CrumblingProgram>>> entry : renderers.entrySet()) {
			IRenderState key = entry.getKey();
			key.bind();

			int width;
			int height;

			ResourceLocation texture = key.getTexture(GlTextureUnit.T0);

			if (texture != null) {
				SheetData atlasData = AtlasInfo.getAtlasData(texture);

				width = atlasData.width;
				height = atlasData.height;
			} else {
				width = height = 256;
			}

			for (MaterialRenderer<CrumblingProgram> materialRenderer : entry.getValue()) {
				materialRenderer.render(layer, translate, camX, camY, camZ, p -> p.setAtlasSize(width, height));
			}

			key.unbind();
		}
	}
}
