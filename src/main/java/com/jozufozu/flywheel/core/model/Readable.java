package com.jozufozu.flywheel.core.model;

import net.minecraft.core.Direction;
import com.mojang.math.Vector3f;

public class Readable {
	public static class ModelBox {
		private final TexturedQuad[] quads;
		public final float posX1;
		public final float posY1;
		public final float posZ1;
		public final float posX2;
		public final float posY2;
		public final float posZ2;

		public ModelBox(int texOffU, int texOffV, float posX1, float posY1, float posZ1, float sizeX, float sizeY, float sizeZ, float growX, float growY, float growZ, boolean mirror, float texWidth, float texHeight) {
			this.posX1 = posX1;
			this.posY1 = posY1;
			this.posZ1 = posZ1;
			this.posX2 = posX1 + sizeX;
			this.posY2 = posY1 + sizeY;
			this.posZ2 = posZ1 + sizeZ;
			this.quads = new TexturedQuad[6];
			float posX2 = posX1 + sizeX;
			float posY2 = posY1 + sizeY;
			float posZ2 = posZ1 + sizeZ;
			posX1 = posX1 - growX;
			posY1 = posY1 - growY;
			posZ1 = posZ1 - growZ;
			posX2 = posX2 + growX;
			posY2 = posY2 + growY;
			posZ2 = posZ2 + growZ;
			if (mirror) {
				float tmp = posX2;
				posX2 = posX1;
				posX1 = tmp;
			}

			PositionTextureVertex lll = new PositionTextureVertex(posX1, posY1, posZ1, 0.0F, 0.0F);
			PositionTextureVertex hll = new PositionTextureVertex(posX2, posY1, posZ1, 0.0F, 8.0F);
			PositionTextureVertex hhl = new PositionTextureVertex(posX2, posY2, posZ1, 8.0F, 8.0F);
			PositionTextureVertex lhl = new PositionTextureVertex(posX1, posY2, posZ1, 8.0F, 0.0F);
			PositionTextureVertex llh = new PositionTextureVertex(posX1, posY1, posZ2, 0.0F, 0.0F);
			PositionTextureVertex hlh = new PositionTextureVertex(posX2, posY1, posZ2, 0.0F, 8.0F);
			PositionTextureVertex hhh = new PositionTextureVertex(posX2, posY2, posZ2, 8.0F, 8.0F);
			PositionTextureVertex lhh = new PositionTextureVertex(posX1, posY2, posZ2, 8.0F, 0.0F);
			float f4 = (float)texOffU;
			float f5 = (float)texOffU + sizeZ;
			float f6 = (float)texOffU + sizeZ + sizeX;
			float f7 = (float)texOffU + sizeZ + sizeX + sizeX;
			float f8 = (float)texOffU + sizeZ + sizeX + sizeZ;
			float f9 = (float)texOffU + sizeZ + sizeX + sizeZ + sizeX;
			float f10 = (float)texOffV;
			float f11 = (float)texOffV + sizeZ;
			float f12 = (float)texOffV + sizeZ + sizeY;
			this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{hlh, llh, lll, hll}, f5, f10, f6, f11, texWidth, texHeight, mirror, Direction.DOWN);
			this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{hhl, lhl, lhh, hhh}, f6, f11, f7, f10, texWidth, texHeight, mirror, Direction.UP);
			this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{lll, llh, lhh, lhl}, f4, f11, f5, f12, texWidth, texHeight, mirror, Direction.WEST);
			this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{hll, lll, lhl, hhl}, f5, f11, f6, f12, texWidth, texHeight, mirror, Direction.NORTH);
			this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{hlh, hll, hhl, hhh}, f6, f11, f8, f12, texWidth, texHeight, mirror, Direction.EAST);
			this.quads[5] = new TexturedQuad(new PositionTextureVertex[]{llh, hlh, hhh, lhh}, f8, f11, f9, f12, texWidth, texHeight, mirror, Direction.SOUTH);
		}
	}

	public static class PositionTextureVertex {
		public final float x;
		public final float y;
		public final float z;
		public final float u;
		public final float v;

		public PositionTextureVertex(float x, float y, float z) {
			this(x, y, z, 0, 0);
		}

		public PositionTextureVertex(float x, float y, float z, float u, float v) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.u = u;
			this.v = v;
		}

		public PositionTextureVertex setTexturePosition(float u, float v) {
			return new PositionTextureVertex(x, y, z, u, v);
		}
	}

	public static class TexturedQuad {
		public final PositionTextureVertex[] vertices;
		public final Vector3f normal;

		public TexturedQuad(PositionTextureVertex[] vertices, float minU, float minV, float maxU, float maxV, float texWidth, float texHeight, boolean p_i225951_8_, Direction p_i225951_9_) {
			this.vertices = vertices;
			float w = 0.0F / texWidth;
			float h = 0.0F / texHeight;
			vertices[0] = vertices[0].setTexturePosition(maxU / texWidth - w, minV / texHeight + h);
			vertices[1] = vertices[1].setTexturePosition(minU / texWidth + w, minV / texHeight + h);
			vertices[2] = vertices[2].setTexturePosition(minU / texWidth + w, maxV / texHeight - h);
			vertices[3] = vertices[3].setTexturePosition(maxU / texWidth - w, maxV / texHeight - h);
			if (p_i225951_8_) {
				int i = vertices.length;

				for(int j = 0; j < i / 2; ++j) {
					PositionTextureVertex modelrenderer$positiontexturevertex = vertices[j];
					vertices[j] = vertices[i - 1 - j];
					vertices[i - 1 - j] = modelrenderer$positiontexturevertex;
				}
			}

			this.normal = p_i225951_9_.step();
			if (p_i225951_8_) {
				this.normal.mul(-1.0F, 1.0F, 1.0F);
			}

		}
	}
}
