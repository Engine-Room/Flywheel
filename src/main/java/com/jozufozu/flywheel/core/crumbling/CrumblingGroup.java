package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.material.MaterialGroup;
import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.core.atlas.AtlasInfo;
import com.jozufozu.flywheel.core.atlas.SheetData;
import com.jozufozu.flywheel.core.shader.IProgramCallback;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class CrumblingGroup<P extends CrumblingProgram> extends MaterialGroup<P> {

	private final int width;
	private final int height;

	public CrumblingGroup(MaterialManager<P> owner, IRenderState state) {
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
	public void render(Matrix4f viewProjection, double camX, double camY, double camZ, IProgramCallback<P> callback) {
		super.render(viewProjection, camX, camY, camZ, ((IProgramCallback<P>) this::setup).andThen(callback));
	}

	private void setup(P p) {
		p.setAtlasSize(width, height);
	}
}
