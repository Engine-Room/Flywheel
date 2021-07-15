package com.jozufozu.flywheel.core.crumbling;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.ArrayList;
import java.util.Map;

import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.MaterialRenderer;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.core.atlas.AtlasInfo;
import com.jozufozu.flywheel.core.atlas.SheetData;
import com.jozufozu.flywheel.core.shader.IProgramCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.container.PlayerContainer;
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

		TextureManager textureManager = Minecraft.getInstance().textureManager;

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureManager.getTexture(PlayerContainer.BLOCK_ATLAS)
				.getId());

		for (MaterialRenderer<CrumblingProgram> material : atlasRenderers) {
			material.render(layer, translate, camX, camY, camZ, CrumblingProgram::setDefaultAtlasSize);
		}

		for (Map.Entry<ResourceLocation, ArrayList<MaterialRenderer<CrumblingProgram>>> entry : renderers.entrySet()) {
			glBindTexture(GL_TEXTURE_2D, textureManager.getTexture(entry.getKey())
					.getId());
			SheetData atlasData = AtlasInfo.getAtlasData(entry.getKey());
			for (MaterialRenderer<CrumblingProgram> materialRenderer : entry.getValue()) {
				materialRenderer.render(layer, translate, camX, camY, camZ, p -> p.setAtlasSize(atlasData.width, atlasData.height));
			}
		}
	}
}
