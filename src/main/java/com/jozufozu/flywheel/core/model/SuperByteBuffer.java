package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.util.ModelReader;
import com.jozufozu.flywheel.util.transform.Rotate;
import com.jozufozu.flywheel.util.transform.Scale;
import com.jozufozu.flywheel.util.transform.TStack;
import com.jozufozu.flywheel.util.transform.Translate;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.*;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class SuperByteBuffer implements Scale<SuperByteBuffer>, Translate<SuperByteBuffer>, Rotate<SuperByteBuffer>, TStack<SuperByteBuffer> {

	private final Model model;
	private final ModelReader template;

	// Vertex Position
	private final PoseStack transforms;

	private final Params defaultParams = Params.defaultParams();
	private final Params params = defaultParams.copy();

	// Temporary
	private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();
	private final Vector4f pos = new Vector4f();
	private final Vector3f normal = new Vector3f();
	private final Vector4f lightPos = new Vector4f();

	public SuperByteBuffer(Model model) {
		this.model = model;
		template = model.getReader();

		transforms = new PoseStack();
		transforms.pushPose();
	}

	public void renderInto(PoseStack input, VertexConsumer builder) {
		if (isEmpty())
			return;

		Matrix4f modelMat = input.last()
				.pose()
				.copy();
		Matrix4f localTransforms = transforms.last()
				.pose();
		modelMat.multiply(localTransforms);

		Matrix3f normalMat;

		if (params.fullNormalTransform) {
			normalMat = input.last().normal().copy();
			normalMat.mul(transforms.last().normal());
		} else {
			normalMat = transforms.last().normal().copy();
		}

		if (params.useWorldLight) {
			WORLD_LIGHT_CACHE.clear();
		}

		float f = .5f;
		int vertexCount = template.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			float x = template.getX(i);
			float y = template.getY(i);
			float z = template.getZ(i);
			pos.set(x, y, z, 1F);
			pos.transform(modelMat);
			builder.vertex(pos.x(), pos.y(), pos.z());

			float normalX = template.getNX(i);
			float normalY = template.getNY(i);
			float normalZ = template.getNZ(i);

			normal.set(normalX, normalY, normalZ);
			normal.transform(normalMat);
			normal.normalize();
			float nx = normal.x();
			float ny = normal.y();
			float nz = normal.z();

			float instanceDiffuse = LightUtil.diffuseLight(nx, ny, nz);

			switch (params.colorMode) {
			case MODEL_ONLY -> builder.color(template.getR(i), template.getG(i), template.getB(i), template.getA(i));
			case DIFFUSE_ONLY -> builder.color(instanceDiffuse, instanceDiffuse, instanceDiffuse, 1f);
			case MODEL_DIFFUSE -> {
				int r = Byte.toUnsignedInt(template.getR(i));
				int g = Byte.toUnsignedInt(template.getG(i));
				int b = Byte.toUnsignedInt(template.getB(i));
				int a = Byte.toUnsignedInt(template.getA(i));

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

			float u = template.getU(i);
			float v = template.getV(i);
			if (params.spriteShiftFunc != null) {
				params.spriteShiftFunc.shift(builder, u, v);
			} else {
				builder.uv(u, v);
			}

			if (params.hasOverlay) {
				builder.overlayCoords(params.overlay);
			}

			int light;
			if (params.useWorldLight) {
				lightPos.set(((x - f) * 15 / 16f) + f, (y - f) * 15 / 16f + f, (z - f) * 15 / 16f + f, 1F);
				lightPos.transform(localTransforms);
				if (params.lightTransform != null) {
					lightPos.transform(params.lightTransform);
				}

				light = getLight(Minecraft.getInstance().level, lightPos);
				if (params.hasCustomLight) {
					light = maxLight(light, params.packedLightCoords);
				}
			} else if (params.hasCustomLight) {
				light = params.packedLightCoords;
			} else {
				light = template.getLight(i);
			}

			if (params.hybridLight) {
				builder.uv2(maxLight(light, template.getLight(i)));
			} else {
				builder.uv2(light);
			}

			builder.normal(nx, ny, nz);

			builder.endVertex();
		}

		reset();
	}

	public SuperByteBuffer reset() {
		while (!transforms.clear())
			transforms.popPose();
		transforms.pushPose();

		params.load(defaultParams);

		return this;
	}

	@Override
	public SuperByteBuffer translate(double x, double y, double z) {
		transforms.translate(x, y, z);
		return this;
	}

	@Override
	public SuperByteBuffer multiply(Quaternion quaternion) {
		transforms.mulPose(quaternion);
		return this;
	}

	public SuperByteBuffer transform(PoseStack stack) {
		PoseStack.Pose last = stack.last();
		return transform(last.pose(), last.normal());
	}

	public SuperByteBuffer transform(Matrix4f pose, Matrix3f normal) {
		transforms.last()
				.pose()
				.multiply(pose);
		transforms.last()
				.normal()
				.mul(normal);
		return this;
	}

	public SuperByteBuffer rotateCentered(Direction axis, float radians) {
		translate(.5f, .5f, .5f).rotate(axis, radians)
			.translate(-.5f, -.5f, -.5f);
		return this;
	}

	public SuperByteBuffer rotateCentered(Quaternion q) {
		translate(.5f, .5f, .5f).multiply(q)
			.translate(-.5f, -.5f, -.5f);
		return this;
	}

	public SuperByteBuffer color(int r, int g, int b, int a) {
		params.colorMode = ColorMode.RECOLOR;
		params.r = r;
		params.g = g;
		params.b = b;
		params.a = a;
		return this;
	}

	public SuperByteBuffer color(byte r, byte g, byte b, byte a) {
		params.colorMode = ColorMode.RECOLOR;
		params.r = Byte.toUnsignedInt(r);
		params.g = Byte.toUnsignedInt(g);
		params.b = Byte.toUnsignedInt(b);
		params.a = Byte.toUnsignedInt(a);
		return this;
	}

	public SuperByteBuffer color(int color) {
		params.colorMode = ColorMode.RECOLOR;
		params.r = ((color >> 16) & 0xFF);
		params.g = ((color >> 8) & 0xFF);
		params.b = (color & 0xFF);
		params.a = 255;
		return this;
	}

	public SuperByteBuffer shiftUV(SpriteShiftFunc entry) {
		params.spriteShiftFunc = entry;
		return this;
	}

	public SuperByteBuffer overlay() {
		params.hasOverlay = true;
		return this;
	}

	public SuperByteBuffer overlay(int overlay) {
		params.hasOverlay = true;
		params.overlay = overlay;
		return this;
	}

	/**
	 * Transforms normals not only by the local matrix stack, but also by the passed matrix stack.
	 */
	public SuperByteBuffer entityMode() {
		params.hasOverlay = true;
		params.fullNormalTransform = true;
		params.diffuseMode = DiffuseMode.NONE;
		params.colorMode = ColorMode.RECOLOR;
		return this;
	}

	public SuperByteBuffer light() {
		params.useWorldLight = true;
		return this;
	}

	public SuperByteBuffer light(Matrix4f lightTransform) {
		params.useWorldLight = true;
		params.lightTransform = lightTransform;
		return this;
	}

	public SuperByteBuffer light(int packedLightCoords) {
		params.hasCustomLight = true;
		params.packedLightCoords = packedLightCoords;
		return this;
	}

	public SuperByteBuffer light(Matrix4f lightTransform, int packedLightCoords) {
		light(lightTransform);
		light(packedLightCoords);
		return this;
	}

	/**
	 * Uses max light from calculated light (world light or custom light) and vertex light for the final light value.
	 * Ineffective if any other light method was not called.
	 */
	public SuperByteBuffer hybridLight() {
		params.hybridLight = true;
		return this;
	}

	public boolean isEmpty() {
		return template.isEmpty();
	}

	@Override
	public SuperByteBuffer scale(float factorX, float factorY, float factorZ) {
		transforms.scale(factorX, factorY, factorZ);
		return this;
	}

	@Override
	public SuperByteBuffer pushPose() {
		transforms.pushPose();
		return this;
	}

	@Override
	public SuperByteBuffer popPose() {
		transforms.popPose();
		return this;
	}

	@Override
	public String toString() {
		return "SuperByteBuffer[" + model + ']';
	}

	public static int transformColor(int component, float scale) {
		return Mth.clamp((int) (component * scale), 0, 255);
	}

	public static int maxLight(int packedLight1, int packedLight2) {
		int blockLight1 = LightTexture.block(packedLight1);
		int skyLight1 = LightTexture.sky(packedLight1);
		int blockLight2 = LightTexture.block(packedLight2);
		int skyLight2 = LightTexture.sky(packedLight2);
		return LightTexture.pack(Math.max(blockLight1, blockLight2), Math.max(skyLight1, skyLight2));
	}

	private static int getLight(Level world, Vector4f lightPos) {
		BlockPos pos = new BlockPos(lightPos.x(), lightPos.y(), lightPos.z());
		return WORLD_LIGHT_CACHE.computeIfAbsent(pos.asLong(), $ -> LevelRenderer.getLightColor(world, pos));
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

	public static class Params {
		// Vertex Coloring
		public ColorMode colorMode = ColorMode.DIFFUSE_ONLY;
		public DiffuseMode diffuseMode = DiffuseMode.INSTANCE;
		public int r;
		public int g;
		public int b;
		public int a;

		// Vertex Texture Coords
		public SpriteShiftFunc spriteShiftFunc;

		// Vertex Overlay Color
		public boolean hasOverlay;
		public int overlay = OverlayTexture.NO_OVERLAY;

		// Vertex Lighting
		public boolean useWorldLight;
		public Matrix4f lightTransform;
		public boolean hasCustomLight;
		public int packedLightCoords;
		public boolean hybridLight;

		// Vertex Normals
		public boolean fullNormalTransform;

		public void load(Params from) {
			colorMode = from.colorMode;
			diffuseMode = from.diffuseMode;
			r = from.r;
			g = from.g;
			b = from.b;
			a = from.a;
			spriteShiftFunc = from.spriteShiftFunc;
			hasOverlay = from.hasOverlay;
			overlay = from.overlay;
			useWorldLight = from.useWorldLight;
			lightTransform = from.lightTransform;
			hasCustomLight = from.hasCustomLight;
			packedLightCoords = from.packedLightCoords;
			hybridLight = from.hybridLight;
			fullNormalTransform = from.fullNormalTransform;
		}

		public Params copy() {
			Params params = new Params();
			params.load(this);
			return params;
		}

		public static Params defaultParams() {
			Params out = new Params();
			out.colorMode = ColorMode.DIFFUSE_ONLY;
			out.diffuseMode = DiffuseMode.INSTANCE;
			out.r = 0xFF;
			out.g = 0xFF;
			out.b = 0xFF;
			out.a = 0xFF;
			out.spriteShiftFunc = null;
			out.hasOverlay = false;
			out.overlay = OverlayTexture.NO_OVERLAY;
			out.useWorldLight = false;
			out.lightTransform = null;
			out.hasCustomLight = false;
			out.packedLightCoords = 0;
			out.hybridLight = false;
			out.fullNormalTransform = false;
			return out;
		}

		public static Params newEntityParams() {
			Params out = new Params();
			out.colorMode = ColorMode.RECOLOR;
			out.diffuseMode = DiffuseMode.NONE;
			out.r = 0xFF;
			out.g = 0xFF;
			out.b = 0xFF;
			out.a = 0xFF;
			out.spriteShiftFunc = null;
			out.hasOverlay = true;
			out.overlay = OverlayTexture.NO_OVERLAY;
			out.useWorldLight = false;
			out.lightTransform = null;
			out.hasCustomLight = false;
			out.packedLightCoords = 0;
			out.hybridLight = false;
			out.fullNormalTransform = true;
			return out;
		}
	}
}
