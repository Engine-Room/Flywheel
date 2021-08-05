package com.jozufozu.flywheel.util;

import com.jozufozu.flywheel.mixin.fabric.VertexFormatAccessor;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.phys.Vec2;

public class BakedQuadWrapper {
	private final FormatCache formatCache = new FormatCache();
	private BakedQuad quad;
	private int[] vertexData;

	public BakedQuadWrapper() {
	}

	public BakedQuadWrapper(BakedQuad quad) {
		this.quad = quad;
		this.vertexData = quad.getVertices();
	}

	public void setQuad(BakedQuad quad) {
		this.quad = quad;
		this.vertexData = this.quad.getVertices();
	}

	public static BakedQuadWrapper of(BakedQuad quad) {
		return new BakedQuadWrapper(quad);
	}

	public void refreshFormat() {
		formatCache.refresh();
	}

	public BakedQuad getQuad() {
		return quad;
	}

	public void clear() {
		quad = null;
		vertexData = null;
	}

	// Getters

	public float getPosX(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.position]);
	}

	public float getPosY(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.position + 1]);
	}

	public float getPosZ(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.position + 2]);
	}

	public Vector3f getPos(int vertexIndex) {
		return new Vector3f(getPosX(vertexIndex), getPosY(vertexIndex), getPosZ(vertexIndex));
	}

	public void copyPos(int vertexIndex, Vector3f pos) {
		pos.set(getPosX(vertexIndex), getPosY(vertexIndex), getPosZ(vertexIndex));
	}

	public int getColor(int vertexIndex) {
		return vertexData[vertexIndex * formatCache.vertexSize + formatCache.color];
	}

	public float getTexU(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.texture]);
	}

	public float getTexV(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.texture + 1]);
	}

	public Vec2 getTex(int vertexIndex) {
		return new Vec2(getTexU(vertexIndex), getTexV(vertexIndex));
	}

	public int getLight(int vertexIndex) {
		return vertexData[vertexIndex * formatCache.vertexSize + formatCache.light];
	}

	public float getNormalX(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal]);
	}

	public float getNormalY(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal + 1]);
	}

	public float getNormalZ(int vertexIndex) {
		return Float.intBitsToFloat(vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal + 2]);
	}

	public Vector3f getNormal(int vertexIndex) {
		return new Vector3f(getNormalX(vertexIndex), getNormalY(vertexIndex), getNormalZ(vertexIndex));
	}

	public void copyNormal(int vertexIndex, Vector3f normal) {
		normal.set(getNormalX(vertexIndex), getNormalY(vertexIndex), getNormalZ(vertexIndex));
	}

	// Setters

	public void setPosX(int vertexIndex, float x) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.position] = Float.floatToRawIntBits(x);
	}

	public void setPosY(int vertexIndex, float y) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.position + 1] = Float.floatToRawIntBits(y);
	}

	public void setPosZ(int vertexIndex, float z) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.position + 2] = Float.floatToRawIntBits(z);
	}

	public void setPos(int vertexIndex, float x, float y, float z) {
		setPosX(vertexIndex, x);
		setPosY(vertexIndex, y);
		setPosZ(vertexIndex, z);
	}

	public void setPos(int vertexIndex, Vector3f pos) {
		setPos(vertexIndex, pos.x(), pos.y(), pos.z());
	}

	public void setColor(int vertexIndex, int color) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.color] = color;
	}

	public void setTexU(int vertexIndex, float u) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.texture] = Float.floatToRawIntBits(u);
	}

	public void setTexV(int vertexIndex, float v) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.texture + 1] = Float.floatToRawIntBits(v);
	}

	public void setTex(int vertexIndex, float u, float v) {
		setTexU(vertexIndex, u);
		setTexV(vertexIndex, v);
	}

	public void setTex(int vertexIndex, Vec2 tex) {
		setTex(vertexIndex, tex.x, tex.y);
	}

	public void setLight(int vertexIndex, int light) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.light] = light;
	}

	public void setNormalX(int vertexIndex, float normalX) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal] = Float.floatToRawIntBits(normalX);
	}

	public void setNormalY(int vertexIndex, float normalY) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal + 1] = Float.floatToRawIntBits(normalY);
	}

	public void setNormalZ(int vertexIndex, float normalZ) {
		vertexData[vertexIndex * formatCache.vertexSize + formatCache.normal + 2] = Float.floatToRawIntBits(normalZ);
	}

	public void setNormal(int vertexIndex, float normalX, float normalY, float normalZ) {
		setNormalX(vertexIndex, normalX);
		setNormalY(vertexIndex, normalY);
		setNormalZ(vertexIndex, normalZ);
	}

	public void setNormal(int vertexIndex, Vector3f normal) {
		setNormal(vertexIndex, normal.x(), normal.y(), normal.z());
	}

	private static class FormatCache {
		private static final VertexFormat FORMAT = DefaultVertexFormat.BLOCK;

		public FormatCache() {
			refresh();
		}

		// Integer size
		public int vertexSize;

		// Element integer offsets
		public int position;
		public int color;
		public int texture;
		public int light;
		public int normal;

		public void refresh() {
			vertexSize = FORMAT.getIntegerSize();
			for (int elementId = 0; elementId < FORMAT.getElements()
					.size(); elementId++) {
				VertexFormatElement element = FORMAT.getElements()
						.get(elementId);
				int intOffset = ((VertexFormatAccessor) FORMAT).getOffsets().getInt(elementId) / Integer.BYTES;
				if (element.getUsage() == VertexFormatElement.Usage.POSITION) {
					position = intOffset;
				} else if (element.getUsage() == VertexFormatElement.Usage.COLOR) {
					color = intOffset;
				} else if (element.getUsage() == VertexFormatElement.Usage.UV) {
					if (element.getIndex() == 0) {
						texture = intOffset;
					} else if (element.getIndex() == 2) {
						light = intOffset;
					}
				} else if (element.getUsage() == VertexFormatElement.Usage.NORMAL) {
					normal = intOffset;
				}
			}
		}
	}
}
