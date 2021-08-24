package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.material.MaterialGroupImpl;
import com.jozufozu.flywheel.backend.material.MaterialManagerImpl;
import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.core.atlas.AtlasInfo;
import com.jozufozu.flywheel.core.atlas.SheetData;

import net.minecraft.util.ResourceLocation;

public class CrumblingGroup<P extends CrumblingProgram> extends MaterialGroupImpl<P> {

	private final int width;
	private final int height;

	public CrumblingGroup(MaterialManagerImpl<P> owner, IRenderState state) {
		super(owner, state);

		ResourceLocation texture = state.getTexture(GlTextureUnit.T0);

		if (texture != null) {
			SheetData atlasData = AtlasInfo.getAtlasData(texture);

			width = atlasData.width;
			height = atlasData.height;
		} else {
			width = height = 256;
		}
	}

	@Override
	public void setup(P p) {
		p.setAtlasSize(width, height);
	}
}
