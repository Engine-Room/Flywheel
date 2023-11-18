package com.jozufozu.flywheel.lib.model.part;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.lib.math.RenderMath;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.vertex.VertexTypes;
import com.mojang.math.Matrix3f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class ModelPartBuilder {
	private final String name;
	private final float textureWidth;
	private final float textureHeight;

	private final List<CuboidBuilder> cuboids = new ArrayList<>();
	@Nullable
	private TextureAtlasSprite sprite;

	public ModelPartBuilder(String name, int textureWidth, int textureHeight) {
		this.name = name;
		this.textureWidth = (float) textureWidth;
		this.textureHeight = (float) textureHeight;
	}

	public ModelPartBuilder sprite(TextureAtlasSprite sprite) {
		this.sprite = sprite;
		return this;
	}

	public CuboidBuilder cuboid() {
		return new CuboidBuilder();
	}

	private ModelPartBuilder addCuboid(CuboidBuilder builder) {
		cuboids.add(builder);
		return this;
	}

	public Mesh build() {
		VertexType vertexType = VertexTypes.POS_TEX_NORMAL;
		int vertices = cuboids.size() * 24;
		MemoryBlock contents = MemoryBlock.malloc(vertexType.getLayout().getStride() * vertices);

		long ptr = contents.ptr();
		VertexWriter writer = new VertexWriter(ptr);
		for (CuboidBuilder cuboid : cuboids) {
			cuboid.write(writer);
		}

		return new SimpleMesh(vertexType, contents, name);
	}

	public class CuboidBuilder {
		private TextureAtlasSprite sprite;

		private int textureOffsetU;
		private int textureOffsetV;

		private float posX1;
		private float posY1;
		private float posZ1;
		private float posX2;
		private float posY2;
		private float posZ2;

		private boolean useRotation;
		private float rotationX;
		private float rotationY;
		private float rotationZ;

		private boolean invertYZ;

		private CuboidBuilder() {
			sprite = ModelPartBuilder.this.sprite;
		}

		public CuboidBuilder sprite(TextureAtlasSprite sprite) {
			this.sprite = sprite;
			return this;
		}

		public CuboidBuilder textureOffset(int u, int v) {
			textureOffsetU = u;
			textureOffsetV = v;
			return this;
		}

		public CuboidBuilder start(float x, float y, float z) {
			posX1 = x;
			posY1 = y;
			posZ1 = z;
			return this;
		}

		public CuboidBuilder end(float x, float y, float z) {
			posX2 = x;
			posY2 = y;
			posZ2 = z;
			return this;
		}

		public CuboidBuilder size(float x, float y, float z) {
			posX2 = posX1 + x;
			posY2 = posY1 + y;
			posZ2 = posZ1 + z;
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
			rotationX = x;
			rotationY = y;
			rotationZ = z;
			return this;
		}

		public CuboidBuilder rotateX(float x) {
			useRotation = true;
			rotationX = x;
			return this;
		}

		public CuboidBuilder rotateY(float y) {
			useRotation = true;
			rotationY = y;
			return this;
		}

		public CuboidBuilder rotateZ(float z) {
			useRotation = true;
			rotationZ = z;
			return this;
		}

		/**
		 * Pulls the cuboid "inside out" through the Y and Z axes.
		 */
		public CuboidBuilder invertYZ() {
			invertYZ = true;
			return this;
		}

		public ModelPartBuilder endCuboid() {
			return ModelPartBuilder.this.addCuboid(this);
		}

		private void write(VertexWriter writer) {
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
				writeQuad(writer, new Vector3f[]{hlh, llh, lll, hll}, f6, f11, f7, f10, down);
				writeQuad(writer, new Vector3f[]{hhl, lhl, lhh, hhh}, f5, f10, f6, f11, up);
				writeQuad(writer, new Vector3f[]{lll, llh, lhh, lhl}, f5, f12, f4, f11, west);
				writeQuad(writer, new Vector3f[]{hll, lll, lhl, hhl}, f9, f12, f8, f11, north);
				writeQuad(writer, new Vector3f[]{hlh, hll, hhl, hhh}, f8, f12, f6, f11, east);
				writeQuad(writer, new Vector3f[]{llh, hlh, hhh, lhh}, f6, f12, f5, f11, south);
			} else {
				writeQuad(writer, new Vector3f[]{hlh, llh, lll, hll}, f5, f10, f6, f11, down);
				writeQuad(writer, new Vector3f[]{hhl, lhl, lhh, hhh}, f6, f11, f7, f10, up);
				writeQuad(writer, new Vector3f[]{lll, llh, lhh, lhl}, f4, f11, f5, f12, west);
				writeQuad(writer, new Vector3f[]{hll, lll, lhl, hhl}, f5, f11, f6, f12, north);
				writeQuad(writer, new Vector3f[]{hlh, hll, hhl, hhh}, f6, f11, f8, f12, east);
				writeQuad(writer, new Vector3f[]{llh, hlh, hhh, lhh}, f8, f11, f9, f12, south);
			}
		}

		private void writeQuad(VertexWriter writer, Vector3f[] vertices, float minU, float minV, float maxU, float maxV, Vector3f normal) {
			writer.putVertex(vertices[0].x(), vertices[0].y(), vertices[0].z(), maxU, minV, normal.x(), normal.y(), normal.z());
			writer.putVertex(vertices[1].x(), vertices[1].y(), vertices[1].z(), minU, minV, normal.x(), normal.y(), normal.z());
			writer.putVertex(vertices[2].x(), vertices[2].y(), vertices[2].z(), minU, maxV, normal.x(), normal.y(), normal.z());
			writer.putVertex(vertices[3].x(), vertices[3].y(), vertices[3].z(), maxU, maxV, normal.x(), normal.y(), normal.z());
		}

		private float getU(float u) {
			if (sprite != null) {
				return sprite.getU(u * 16 / textureWidth);
			} else {
				return u / textureWidth;
			}
		}

		private float getV(float v) {
			if (sprite != null) {
				return sprite.getV(v * 16 / textureHeight);
			} else {
				return v / textureHeight;
			}
		}
	}

	private static class VertexWriter {
		private long ptr;

		public VertexWriter(long ptr) {
			this.ptr = ptr;
		}

		public void putVertex(float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
			MemoryUtil.memPutFloat(ptr, x);
			MemoryUtil.memPutFloat(ptr + 4, y);
			MemoryUtil.memPutFloat(ptr + 8, z);
			MemoryUtil.memPutFloat(ptr + 12, u);
			MemoryUtil.memPutFloat(ptr + 16, v);
			MemoryUtil.memPutByte(ptr + 20, RenderMath.nb(nX));
			MemoryUtil.memPutByte(ptr + 21, RenderMath.nb(nY));
			MemoryUtil.memPutByte(ptr + 22, RenderMath.nb(nZ));

			ptr += 23;
		}
	}
}
