package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.util.RenderMath;
import com.jozufozu.flywheel.util.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class ModelTransformer {

	private final Model model;
	private final VertexList reader;

	public final Context context = new Context();

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
		if (context.fullNormalTransform) {
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

			if (params.useParamColor) {
				if (context.outputColorDiffuse) {
					float instanceDiffuse = LightUtil.diffuseLight(nx, ny, nz);
					int colorR = transformColor(params.r, instanceDiffuse);
					int colorG = transformColor(params.g, instanceDiffuse);
					int colorB = transformColor(params.b, instanceDiffuse);
					builder.color(colorR, colorG, colorB, params.a);
				} else {
					builder.color(params.r, params.g, params.b, params.a);
				}
			} else {
				if (context.outputColorDiffuse) {
					int d = RenderMath.unb(LightUtil.diffuseLight(nx, ny, nz));
					builder.color(d, d, d, 0xFF);
				} else {
					builder.color(reader.getR(i), reader.getG(i), reader.getB(i), reader.getA(i));
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

			// not always used, but will be ignored by formats that don't use it
			builder.overlayCoords(params.overlay);

			builder.uv2(params.useParamLight ? params.packedLightCoords : reader.getLight(i));

			builder.normal(nx, ny, nz);

			builder.endVertex();
		}
	}

	public boolean isEmpty() {
		return reader.isEmpty();
	}

	@Override
	public String toString() {
		return "ModelTransformer[" + model + ']';
	}

	public static int transformColor(int component, float scale) {
		return Mth.clamp((int) (component * scale), 0, 255);
	}

	@FunctionalInterface
	public interface SpriteShiftFunc {
		void shift(VertexConsumer builder, float u, float v);
	}

	public static class Context {
		/**
		 * Do we need to include the PoseStack transforms in our transformation of the normal?
		 */
		public boolean fullNormalTransform = false;

		/**
		 * Do we need to bake diffuse lighting into the output colors?
		 */
		public boolean outputColorDiffuse = true;

	}

	public static class Params implements Transform<Params> {

		// Transform
		public final Matrix4f model;
		public final Matrix3f normal;

		// Vertex Coloring
		public boolean useParamColor;
		public int r;
		public int g;
		public int b;
		public int a;

		// Vertex Texture Coords
		public SpriteShiftFunc spriteShiftFunc;

		// Vertex Overlay Color
		public int overlay;

		// Vertex Lighting
		public boolean useParamLight;
		public int packedLightCoords;

		public Params() {
			model = new Matrix4f();
			normal = new Matrix3f();
		}

		public void loadDefault() {
			model.setIdentity();
			normal.setIdentity();
			useParamColor = true;
			r = 0xFF;
			g = 0xFF;
			b = 0xFF;
			a = 0xFF;
			spriteShiftFunc = null;
			overlay = OverlayTexture.NO_OVERLAY;
			useParamLight = false;
			packedLightCoords = LightTexture.FULL_BRIGHT;
		}

		public void load(Params from) {
			model.load(from.model);
			normal.load(from.normal);
			useParamColor = from.useParamColor;
			r = from.r;
			g = from.g;
			b = from.b;
			a = from.a;
			spriteShiftFunc = from.spriteShiftFunc;
			overlay = from.overlay;
			useParamLight = from.useParamLight;
			packedLightCoords = from.packedLightCoords;
		}

		public Params color(int r, int g, int b, int a) {
			this.useParamColor = true;
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
			return this;
		}

		public Params color(byte r, byte g, byte b, byte a) {
			this.useParamColor = true;
			this.r = Byte.toUnsignedInt(r);
			this.g = Byte.toUnsignedInt(g);
			this.b = Byte.toUnsignedInt(b);
			this.a = Byte.toUnsignedInt(a);
			return this;
		}

		public Params color(int color) {
			this.useParamColor = true;
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

		public Params overlay(int overlay) {
			this.overlay = overlay;
			return this;
		}

		public Params light(int packedLightCoords) {
			this.useParamLight = true;
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

	}
}
