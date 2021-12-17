package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.util.ModelReader;
import com.jozufozu.flywheel.util.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.*;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class ModelTransformer {

	private final Model model;
	private final ModelReader reader;

	public ModelTransformer(Model model) {
		this.model = model;
		reader = model.getReader();
	}

	public void renderInto(Params params, PoseStack input, VertexConsumer builder) {
		if (isEmpty())
			return;

		Vector4f pos = new Vector4f();
		Vector3f normal = new Vector3f();

		Matrix4f modelMat = input.last()
				.pose()
				.copy();
		modelMat.multiply(params.model);

		Matrix3f normalMat;
		if (params.fullNormalTransform) {
			normalMat = input.last().normal().copy();
			normalMat.mul(params.normal);
		} else {
			normalMat = params.normal.copy();
		}

		int vertexCount = reader.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			float x = reader.getX(i);
			float y = reader.getY(i);
			float z = reader.getZ(i);
			pos.set(x, y, z, 1F);
			pos.transform(modelMat);
			builder.vertex(pos.x(), pos.y(), pos.z());

			float normalX = reader.getNX(i);
			float normalY = reader.getNY(i);
			float normalZ = reader.getNZ(i);

			normal.set(normalX, normalY, normalZ);
			normal.transform(normalMat);
			normal.normalize();
			float nx = normal.x();
			float ny = normal.y();
			float nz = normal.z();

			float instanceDiffuse = LightUtil.diffuseLight(nx, ny, nz);

			switch (params.colorMode) {
			case MODEL_ONLY -> builder.color(reader.getR(i), reader.getG(i), reader.getB(i), reader.getA(i));
			case DIFFUSE_ONLY -> builder.color(instanceDiffuse, instanceDiffuse, instanceDiffuse, 1f);
			case MODEL_DIFFUSE -> {
				int r = Byte.toUnsignedInt(reader.getR(i));
				int g = Byte.toUnsignedInt(reader.getG(i));
				int b = Byte.toUnsignedInt(reader.getB(i));
				int a = Byte.toUnsignedInt(reader.getA(i));

				float diffuse = switch (params.diffuseMode) {
				case NONE -> 1f;
				case INSTANCE -> instanceDiffuse;
				case ONE_OVER_STATIC -> 1 / LightUtil.diffuseLight(normalX, normalY, normalZ);
				case INSTANCE_OVER_STATIC -> instanceDiffuse / LightUtil.diffuseLight(normalX, normalY, normalZ);
				};

				if (diffuse != 1) {
					r = transformColor(r, diffuse);
					g = transformColor(g, diffuse);
					b = transformColor(b, diffuse);
				}

				builder.color(r, g, b, a);
			}
			case RECOLOR -> {
				if (params.diffuseMode == DiffuseMode.NONE) {
					builder.color(params.r, params.g, params.b, params.a);
				} else {
					int colorR = transformColor(params.r, instanceDiffuse);
					int colorG = transformColor(params.g, instanceDiffuse);
					int colorB = transformColor(params.b, instanceDiffuse);
					builder.color(colorR, colorG, colorB, params.a);
				}
			}
			}

			//builder.color(Math.max(0, (int) (nx * 255)), Math.max(0, (int) (ny * 255)), Math.max(0, (int) (nz * 255)), 0xFF);
			//builder.color(Math.max(0, (int) (normalX * 255)), Math.max(0, (int) (normalY * 255)), Math.max(0, (int) (normalZ * 255)), 0xFF);

			float u = reader.getU(i);
			float v = reader.getV(i);
			if (params.spriteShiftFunc != null) {
				params.spriteShiftFunc.shift(builder, u, v);
			} else {
				builder.uv(u, v);
			}

			if (params.hasOverlay) {
				builder.overlayCoords(params.overlay);
			}

			builder.uv2(params.hasCustomLight ? params.packedLightCoords : reader.getLight(i));

			builder.normal(nx, ny, nz);

			builder.endVertex();
		}
	}

	public boolean isEmpty() {
		return reader.isEmpty();
	}

	@Override
	public String toString() {
		return "SuperByteBuffer[" + model + ']';
	}

	public static int transformColor(int component, float scale) {
		return Mth.clamp((int) (component * scale), 0, 255);
	}

	@FunctionalInterface
	public interface SpriteShiftFunc {
		void shift(VertexConsumer builder, float u, float v);
	}

	public enum ColorMode {
		MODEL_ONLY,
		MODEL_DIFFUSE,
		DIFFUSE_ONLY,
		RECOLOR
	}

	public enum DiffuseMode {
		NONE,
		INSTANCE,
		ONE_OVER_STATIC,
		INSTANCE_OVER_STATIC,
	}

	public static class Params implements Transform<Params> {
		// Vertex Position
		public final Matrix4f model;
		public final Matrix3f normal;

		// Vertex Coloring
		public ColorMode colorMode;
		public DiffuseMode diffuseMode;
		public int r;
		public int g;
		public int b;
		public int a;

		// Vertex Texture Coords
		public SpriteShiftFunc spriteShiftFunc;

		// Vertex Overlay Color
		public boolean hasOverlay;
		public int overlay;

		// Vertex Lighting
		public boolean hasCustomLight;
		public int packedLightCoords;

		// Vertex Normals
		public boolean fullNormalTransform;

		public Params() {
			model = new Matrix4f();
			normal = new Matrix3f();
		}

		public void load(Params from) {
			model.load(from.model);
			normal.load(from.normal);
			colorMode = from.colorMode;
			diffuseMode = from.diffuseMode;
			r = from.r;
			g = from.g;
			b = from.b;
			a = from.a;
			spriteShiftFunc = from.spriteShiftFunc;
			hasOverlay = from.hasOverlay;
			overlay = from.overlay;
			hasCustomLight = from.hasCustomLight;
			packedLightCoords = from.packedLightCoords;
			fullNormalTransform = from.fullNormalTransform;
		}

		public Params copy() {
			Params params = new Params();
			params.load(this);
			return params;
		}

		public Params color(int r, int g, int b, int a) {
			this.colorMode = ColorMode.RECOLOR;
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
			return this;
		}

		public Params color(byte r, byte g, byte b, byte a) {
			this.colorMode = ColorMode.RECOLOR;
			this.r = Byte.toUnsignedInt(r);
			this.g = Byte.toUnsignedInt(g);
			this.b = Byte.toUnsignedInt(b);
			this.a = Byte.toUnsignedInt(a);
			return this;
		}

		public Params color(int color) {
			this.colorMode = ColorMode.RECOLOR;
			this.r = ((color >> 16) & 0xFF);
			this.g = ((color >> 8) & 0xFF);
			this.b = (color & 0xFF);
			this.a = 255;
			return this;
		}

		public Params shiftUV(SpriteShiftFunc entry) {
			this.spriteShiftFunc = entry;
			return this;
		}

		public Params overlay() {
			this.hasOverlay = true;
			return this;
		}

		public Params overlay(int overlay) {
			this.hasOverlay = true;
			this.overlay = overlay;
			return this;
		}

		/**
		 * Transforms normals not only by the local matrix stack, but also by the passed matrix stack.
		 */
		public void entityMode() {
			this.hasOverlay = true;
			this.fullNormalTransform = true;
			this.diffuseMode = DiffuseMode.NONE;
			this.colorMode = ColorMode.RECOLOR;
		}

		public Params light(int packedLightCoords) {
			this.hasCustomLight = true;
			this.packedLightCoords = packedLightCoords;
			return this;
		}

		@Override
		public Params multiply(Quaternion quaternion) {
			model.multiply(quaternion);
			normal.mul(quaternion);
			return this;
		}

		@Override
		public Params scale(float pX, float pY, float pZ) {
			model.multiply(Matrix4f.createScaleMatrix(pX, pY, pZ));
			if (pX == pY && pY == pZ) {
				if (pX > 0.0F) {
					return this;
				}

				normal.mul(-1.0F);
			}

			float f = 1.0F / pX;
			float f1 = 1.0F / pY;
			float f2 = 1.0F / pZ;
			float f3 = Mth.fastInvCubeRoot(f * f1 * f2);
			normal.mul(Matrix3f.createScaleMatrix(f3 * f, f3 * f1, f3 * f2));
			return this;
		}

		@Override
		public Params translate(double x, double y, double z) {
			model.multiplyWithTranslation((float) x, (float) y, (float) z);

			return this;
		}

		@Override
		public Params mulPose(Matrix4f pose) {
			this.model.multiply(pose);
			return this;
		}

		@Override
		public Params mulNormal(Matrix3f normal) {
			this.normal.mul(normal);
			return this;
		}

		public static Params defaultParams() {
			Params out = new Params();
			out.model.setIdentity();
			out.normal.setIdentity();
			out.colorMode = ColorMode.DIFFUSE_ONLY;
			out.diffuseMode = DiffuseMode.INSTANCE;
			out.r = 0xFF;
			out.g = 0xFF;
			out.b = 0xFF;
			out.a = 0xFF;
			out.spriteShiftFunc = null;
			out.hasOverlay = false;
			out.overlay = OverlayTexture.NO_OVERLAY;
			out.hasCustomLight = false;
			out.packedLightCoords = LightTexture.FULL_BRIGHT;
			out.fullNormalTransform = false;
			return out;
		}
	}
}
