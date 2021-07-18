package com.jozufozu.flywheel.core.model;

import static com.jozufozu.flywheel.util.RenderMath.nb;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.backend.model.IndexedModel;
import com.jozufozu.flywheel.core.Formats;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class PartBuilder {

	private float sizeU = 64.0F;
	private float sizeV = 32.0F;

	private TextureAtlasSprite sprite;

	private final List<CuboidBuilder> cuboids = new ArrayList<>();

	public PartBuilder() { }

	public PartBuilder(int sizeU, int sizeV) {
		this.setTextureSize(sizeU, sizeV);
	}

	public PartBuilder setTextureSize(int textureWidth, int textureHeight) {
		this.sizeU = (float)textureWidth;
		this.sizeV = (float)textureHeight;
		return this;
	}

	public PartBuilder sprite(TextureAtlasSprite sprite) {
		this.sprite = sprite;
		return this;
	}

	public CuboidBuilder cuboid() {
		return new CuboidBuilder(this);
	}

	public BufferedModel build() {
		int vertices = 0;

		for (CuboidBuilder cuboid : cuboids) {
			vertices += cuboid.vertices();
		}

		VecBuffer buffer = VecBuffer.allocate(vertices * Formats.UNLIT_MODEL.getStride());

		for (CuboidBuilder cuboid : cuboids) {
			cuboid.buffer(buffer);
		}

		buffer.rewind();

		return IndexedModel.fromSequentialQuads(Formats.UNLIT_MODEL, buffer.unwrap(), vertices);
	}

	private PartBuilder addCuboid(CuboidBuilder builder) {
		cuboids.add(builder);
		return this;
	}

	public static class CuboidBuilder {

		TextureAtlasSprite sprite;

		Set<Direction> visibleFaces = EnumSet.allOf(Direction.class);
		int textureOffsetU;
		int textureOffsetV;

		float posX1;
		float posY1;
		float posZ1;
		float posX2;
		float posY2;
		float posZ2;

		boolean invertYZ;

		final PartBuilder partBuilder;

		CuboidBuilder(PartBuilder partBuilder) {
			this.partBuilder = partBuilder;
			this.sprite = partBuilder.sprite;
		}

		public CuboidBuilder textureOffset(int u, int v) {
			this.textureOffsetU = u;
			this.textureOffsetV = v;
			return this;
		}

		public CuboidBuilder start(float x, float y, float z) {
			this.posX1 = x;
			this.posY1 = y;
			this.posZ1 = z;
			return this;
		}

		public CuboidBuilder end(float x, float y, float z) {
			this.posX2 = x;
			this.posY2 = y;
			this.posZ2 = z;
			return this;
		}

		public CuboidBuilder size(float x, float y, float z) {
			this.posX2 = posX1 + x;
			this.posY2 = posY1 + y;
			this.posZ2 = posZ1 + z;
			return this;
		}

		public CuboidBuilder sprite(TextureAtlasSprite sprite) {
			this.sprite = sprite;
			return this;
		}

		/**
		 * Pulls the cuboid "inside out" through the Y and Z axes.
		 */
		public CuboidBuilder invertYZ() {
			this.invertYZ = true;
			return this;
		}

		public PartBuilder endCuboid() {
			return partBuilder.addCuboid(this);
		}

		public int vertices() {
			return visibleFaces.size() * 4;
		}

		public void buffer(VecBuffer buffer) {

			float sizeX = posX2 - posX1;
			float sizeY = posY2 - posY1;
			float sizeZ = posZ2 - posZ1;

			float posX1 = this.posX1 / 16f;
			float posY1 = this.posY1 / 16f;
			float posZ1 = this.posZ1 / 16f;
			float posX2 = this.posX2 / 16f;
			float posY2 = this.posY2 / 16f;
			float posZ2 = this.posZ2 / 16f;

			Vector3f lll = new Vector3f(posX1, posY1, posZ1);
			Vector3f hll = new Vector3f(posX2, posY1, posZ1);
			Vector3f hhl = new Vector3f(posX2, posY2, posZ1);
			Vector3f lhl = new Vector3f(posX1, posY2, posZ1);
			Vector3f llh = new Vector3f(posX1, posY1, posZ2);
			Vector3f hlh = new Vector3f(posX2, posY1, posZ2);
			Vector3f hhh = new Vector3f(posX2, posY2, posZ2);
			Vector3f lhh = new Vector3f(posX1, posY2, posZ2);
			float f4 = getU((float)textureOffsetU);
			float f5 = getU((float)textureOffsetU + sizeZ);
			float f6 = getU((float)textureOffsetU + sizeZ + sizeX);
			float f7 = getU((float)textureOffsetU + sizeZ + sizeX + sizeX);
			float f8 = getU((float)textureOffsetU + sizeZ + sizeX + sizeZ);
			float f9 = getU((float)textureOffsetU + sizeZ + sizeX + sizeZ + sizeX);
			float f10 = getV((float)textureOffsetV);
			float f11 = getV((float)textureOffsetV + sizeZ);
			float f12 = getV((float)textureOffsetV + sizeZ + sizeY);

			if (invertYZ) {
				quad(buffer, new Vector3f[]{hlh, llh, lll, hll}, f6, f11, f7, f10, Direction.DOWN);
				quad(buffer, new Vector3f[]{hhl, lhl, lhh, hhh}, f5, f10, f6, f11, Direction.UP);
				quad(buffer, new Vector3f[]{lll, llh, lhh, lhl}, f5, f12, f4, f11, Direction.WEST);
				quad(buffer, new Vector3f[]{hll, lll, lhl, hhl}, f9, f12, f8, f11, Direction.NORTH);
				quad(buffer, new Vector3f[]{hlh, hll, hhl, hhh}, f8, f12, f6, f11, Direction.EAST);
				quad(buffer, new Vector3f[]{llh, hlh, hhh, lhh}, f6, f12, f5, f11, Direction.SOUTH);
			} else {
				quad(buffer, new Vector3f[]{hlh, llh, lll, hll}, f5, f10, f6, f11, Direction.DOWN);
				quad(buffer, new Vector3f[]{hhl, lhl, lhh, hhh}, f6, f11, f7, f10, Direction.UP);
				quad(buffer, new Vector3f[]{lll, llh, lhh, lhl}, f4, f11, f5, f12, Direction.WEST);
				quad(buffer, new Vector3f[]{hll, lll, lhl, hhl}, f5, f11, f6, f12, Direction.NORTH);
				quad(buffer, new Vector3f[]{hlh, hll, hhl, hhh}, f6, f11, f8, f12, Direction.EAST);
				quad(buffer, new Vector3f[]{llh, hlh, hhh, lhh}, f8, f11, f9, f12, Direction.SOUTH);
			}
		}


		public void quad(VecBuffer buffer, Vector3f[] vertices, float minU, float minV, float maxU, float maxV, Direction dir) {

			Vector3f normal = dir.step();

			buffer.putVec3(vertices[0].x(), vertices[0].y(), vertices[0].z()).putVec3(nb(normal.x()), nb(normal.y()), nb(normal.z())).putVec2(maxU, minV);
			buffer.putVec3(vertices[1].x(), vertices[1].y(), vertices[1].z()).putVec3(nb(normal.x()), nb(normal.y()), nb(normal.z())).putVec2(minU, minV);
			buffer.putVec3(vertices[2].x(), vertices[2].y(), vertices[2].z()).putVec3(nb(normal.x()), nb(normal.y()), nb(normal.z())).putVec2(minU, maxV);
			buffer.putVec3(vertices[3].x(), vertices[3].y(), vertices[3].z()).putVec3(nb(normal.x()), nb(normal.y()), nb(normal.z())).putVec2(maxU, maxV);

		}

		public float getU(float u) {
			if (sprite != null)
				return sprite.getU(u * 16 / partBuilder.sizeU);
			else
				return u;
		}

		public float getV(float v) {
			if (sprite != null)
				return sprite.getV(v * 16 / partBuilder.sizeV);
			else
				return v;
		}
	}


}
