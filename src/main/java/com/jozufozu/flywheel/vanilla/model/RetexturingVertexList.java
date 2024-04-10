package com.jozufozu.flywheel.vanilla.model;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.lib.vertex.WrappedVertexList;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * A wrapper so that differently textured models from the same mesh tree can share the same backing memory
 */
public class RetexturingVertexList extends WrappedVertexList {
	private final TextureAtlasSprite sprite;

	public RetexturingVertexList(MutableVertexList delegate, TextureAtlasSprite sprite) {
		super(delegate);

		this.sprite = sprite;
	}

	@Override
	public void u(int index, float u) {
		super.u(index, sprite.getU(u * 16));
	}

	@Override
	public void v(int index, float v) {
		super.v(index, sprite.getV(v * 16));
	}
}
