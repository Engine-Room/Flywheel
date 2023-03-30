package com.jozufozu.flywheel.lib.modelpart;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.mojang.math.Matrix3f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class PartBuilder {

	private final float sizeU;
	private final float sizeV;

	private TextureAtlasSprite sprite;

	private final List<CuboidBuilder> cuboids = new ArrayList<>();
	private final String name;

	public PartBuilder(String name, int sizeU, int sizeV) {
		this.name = name;
		this.sizeU = (float) sizeU;
		this.sizeV = (float) sizeV;
	}

	public PartBuilder sprite(TextureAtlasSprite sprite) {
		this.sprite = sprite;
		return this;
	}

	public CuboidBuilder cuboid() {
		return new CuboidBuilder(this);
	}

	public ModelPart build() {
		return new ModelPart(cuboids, name);
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

		boolean useRotation;
		float rotationX;
		float rotationY;
		float rotationZ;

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

		public CuboidBuilder shift(float x, float y, float z) {
			posX1 = posX1 - x;
			posY1 = posY1 - y;
			posZ1 = posZ1 - z;
			posX2 = posX2 - x;
			posY2 = posY2 - y;
			posZ2 = posZ2 - z;
			return this;
		}

		public CuboidBuilder rotate(float x, float y, float z) {
			useRotation = true;
			this.rotationX = x;
			this.rotationY = y;
			this.rotationZ = z;
			return this;
		}

		public CuboidBuilder rotateX(float x) {
			useRotation = true;
			this.rotationX = x;
			return this;
		}

		public CuboidBuilder rotateY(float y) {
			useRotation = true;
			this.rotationY = y;
			return this;
		}

		public CuboidBuilder rotateZ(float z) {
			useRotation = true;
			this.rotationZ = z;
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

		public void write(VertexWriter writer) {
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

			Vector3f down = Direction.DOWN.step();
			Vector3f up = Direction.UP.step();
			Vector3f west = Direction.WEST.step();
			Vector3f north = Direction.NORTH.step();
			Vector3f east = Direction.EAST.step();
			Vector3f south = Direction.SOUTH.step();

			if (useRotation) {
				Matrix3f matrix3f = new Matrix3f(new Quaternion(rotationX, rotationY, rotationZ, false));
				lll.transform(matrix3f);
				hll.transform(matrix3f);
				hhl.transform(matrix3f);
				lhl.transform(matrix3f);
				llh.transform(matrix3f);
				hlh.transform(matrix3f);
				hhh.transform(matrix3f);
				lhh.transform(matrix3f);
				down.transform(matrix3f);
				up.transform(matrix3f);
				west.transform(matrix3f);
				north.transform(matrix3f);
				east.transform(matrix3f);
				south.transform(matrix3f);
			}

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
				quad(writer, new Vector3f[]{hlh, llh, lll, hll}, f6, f11, f7, f10, down);
				quad(writer, new Vector3f[]{hhl, lhl, lhh, hhh}, f5, f10, f6, f11, up);
				quad(writer, new Vector3f[]{lll, llh, lhh, lhl}, f5, f12, f4, f11, west);
				quad(writer, new Vector3f[]{hll, lll, lhl, hhl}, f9, f12, f8, f11, north);
				quad(writer, new Vector3f[]{hlh, hll, hhl, hhh}, f8, f12, f6, f11, east);
				quad(writer, new Vector3f[]{llh, hlh, hhh, lhh}, f6, f12, f5, f11, south);
			} else {
				quad(writer, new Vector3f[]{hlh, llh, lll, hll}, f5, f10, f6, f11, down);
				quad(writer, new Vector3f[]{hhl, lhl, lhh, hhh}, f6, f11, f7, f10, up);
				quad(writer, new Vector3f[]{lll, llh, lhh, lhl}, f4, f11, f5, f12, west);
				quad(writer, new Vector3f[]{hll, lll, lhl, hhl}, f5, f11, f6, f12, north);
				quad(writer, new Vector3f[]{hlh, hll, hhl, hhh}, f6, f11, f8, f12, east);
				quad(writer, new Vector3f[]{llh, hlh, hhh, lhh}, f8, f11, f9, f12, south);
			}
		}

		public void quad(VertexWriter writer, Vector3f[] vertices, float minU, float minV, float maxU, float maxV, Vector3f normal) {
			writer.putVertex(vertices[0].x(), vertices[0].y(), vertices[0].z(), maxU, minV, normal.x(), normal.y(), normal.z());
			writer.putVertex(vertices[1].x(), vertices[1].y(), vertices[1].z(), minU, minV, normal.x(), normal.y(), normal.z());
			writer.putVertex(vertices[2].x(), vertices[2].y(), vertices[2].z(), minU, maxV, normal.x(), normal.y(), normal.z());
			writer.putVertex(vertices[3].x(), vertices[3].y(), vertices[3].z(), maxU, maxV, normal.x(), normal.y(), normal.z());
		}

		public float getU(float u) {
			if (sprite != null)
				return sprite.getU(u * 16 / partBuilder.sizeU);
			else
				return u / partBuilder.sizeU;
		}

		public float getV(float v) {
			if (sprite != null)
				return sprite.getV(v * 16 / partBuilder.sizeV);
			else
				return v / partBuilder.sizeV;
		}
	}

}
