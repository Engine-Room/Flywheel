package dev.engine_room.vanillin.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.mojang.blaze3d.font.GlyphInfo;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.lib.model.QuadMesh;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.vanillin.GlyphInstance;
import dev.engine_room.vanillin.VanillinInstanceTypes;
import dev.engine_room.vanillin.mixin.text.FontSetAccessor;
import io.netty.util.internal.ThreadLocalRandom;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;

/**
 * A visual that renders a single line of text.
 */
public final class TextVisual {
	private static final Font FONT = Minecraft.getInstance().font;

	private static final RendererReloadCache<GlyphMeshKey, GlyphMesh> GLYPH_MESH_CACHE = new RendererReloadCache<>(GlyphMeshKey::into);
	private static final RendererReloadCache<GlyphModelKey, Model> GLYPH_MODEL_CACHE = new RendererReloadCache<>(GlyphModelKey::into);

	public final float width;

	@Nullable
	private final BakedGlyphs glyphs;
	@Nullable
	private final EffectGlyphs effectGlyphs;
	@Nullable
	private final ObfuscatedGlyphs obfuscatedGlyphs;

	private final InstancerProvider instancerProvider;

	public TextVisual(InstancerProvider instancerProvider, FormattedCharSequence textLine, List<TextLayer> layers) {
		this.instancerProvider = instancerProvider;

		var sink = new Sink(instancerProvider, layers);

		textLine.accept(sink);

		this.width = sink.x;

		this.glyphs = sink.glyphs;
		this.effectGlyphs = sink.effectGlyphs;
		this.obfuscatedGlyphs = sink.obfuscatedGlyphs;

		// Nuke unused memory, may not actually be necessary.
		if (glyphs != null) {
			glyphs.shrinkToFit();
		}
		if (effectGlyphs != null) {
			effectGlyphs.shrinkToFit();
		}
		if (obfuscatedGlyphs != null) {
			obfuscatedGlyphs.shrinkToFit();
		}
	}

	public void updatePose(Matrix4f pose) {
		if (glyphs != null) {
			glyphs.updatePose(pose);
		}
		if (effectGlyphs != null) {
			effectGlyphs.updatePose(pose);
		}
		if (obfuscatedGlyphs != null) {
			obfuscatedGlyphs.updatePose(instancerProvider, pose);
		}
	}

	public void updateObfuscated() {
		if (obfuscatedGlyphs != null) {
			obfuscatedGlyphs.update(instancerProvider);
		}
	}

	public void updateLight(int packedLight) {
		if (glyphs != null) {
			glyphs.updateLight(packedLight);
		}
		if (effectGlyphs != null) {
			effectGlyphs.updateLight(packedLight);
		}
		if (obfuscatedGlyphs != null) {
			obfuscatedGlyphs.updateLight(packedLight);
		}
	}

	public void delete() {
		if (glyphs != null) {
			glyphs.delete();
		}
		if (effectGlyphs != null) {
			effectGlyphs.delete();
		}
		if (obfuscatedGlyphs != null) {
			obfuscatedGlyphs.delete();
		}
	}

	private record GlyphMeshKey(float glyphWidth, float glyphHeight, TextLayer.GlyphPattern pattern, float boldOffset, float shadowOffset) {
		public GlyphMesh into() {
			List<Vector2fc> out = new ArrayList<>();

			pattern.addGlyphs(offsetc -> {
				Vector2f offset = new Vector2f(offsetc).mul(shadowOffset);
				out.add(offset);

				if (boldOffset != 0.0f) {
					out.add(new Vector2f(offset.x() + boldOffset, offset.y()));
				}
			});

			return new GlyphMesh(glyphWidth, glyphHeight, out.toArray(Vector2fc[]::new));
		}
	}

	private record GlyphModelKey(@Nullable GlyphMeshKey meshKey, TextLayer.GlyphMaterial material, ResourceLocation texture) {
		public Model into() {
			Mesh mesh;

			if (meshKey != null) {
				mesh = GLYPH_MESH_CACHE.get(meshKey);
			} else {
				mesh = GlyphEffectMesh.INSTANCE;
			}

			return new SingleMeshModel(mesh, material.create(texture));
		}
	}

	/**
	 * Static glyphs. One entry for every glyph, times the number of layers.
	 */
	private static class BakedGlyphs {
		private static final int INITIAL_CAPACITY = 16;
		public int count = 0;

		// Array lengths must be kept in sync.
		public float[] x = new float[INITIAL_CAPACITY];
		public float[] y = new float[INITIAL_CAPACITY];
		public float[] left = new float[INITIAL_CAPACITY];
		public float[] up = new float[INITIAL_CAPACITY];
		public boolean[] italic = new boolean[INITIAL_CAPACITY];
		public GlyphInstance[] instance = new GlyphInstance[INITIAL_CAPACITY];

		public void delete() {
			for (int i = 0; i < count; i++) {
				instance[i].delete();
				instance[i] = null;
			}
			count = 0;
		}

		public void updatePose(Matrix4f pose) {
			for (int i = 0; i < count; i++) {
				instance[i].updatePose(pose, x[i], y[i], left[i], up[i], italic[i]);
				instance[i].setChanged();
			}
		}

		public void add(float x, float y, float left, float up, boolean italic, GlyphInstance instance) {
			if (count >= capacity()) {
				reallocate(Math.max(2, capacity()) * 2);
			}

			this.x[count] = x;
			this.y[count] = y;
			this.left[count] = left;
			this.up[count] = up - 3.0f;
			this.italic[count] = italic;
			this.instance[count] = instance;

			count++;
		}

		private int capacity() {
			return x.length;
		}

		private void reallocate(int capacity) {
			x = Arrays.copyOf(x, capacity);
			y = Arrays.copyOf(y, capacity);
			left = Arrays.copyOf(left, capacity);
			up = Arrays.copyOf(up, capacity);
			italic = Arrays.copyOf(italic, capacity);
			instance = Arrays.copyOf(instance, capacity);
		}

		private void shrinkToFit() {
			reallocate(count);
		}

		public void updateLight(int packedLight) {
			for (int i = 0; i < count; i++) {
				instance[i].light(packedLight);
				instance[i].setChanged();
			}
		}
	}


	/**
	 * Effect glyphs. One entry for every effect, times the number of layers.
	 */
	private static class EffectGlyphs {
		private static final int INITIAL_CAPACITY = 16;
		private int count = 0;

		// Array lengths must be kept in sync.
		private float[] x0 = new float[INITIAL_CAPACITY];
		private float[] y0 = new float[INITIAL_CAPACITY];
		private float[] width = new float[INITIAL_CAPACITY];
		private float[] height = new float[INITIAL_CAPACITY];
		private float[] depth = new float[INITIAL_CAPACITY];
		private GlyphInstance[] instance = new GlyphInstance[INITIAL_CAPACITY];

		public void delete() {
			for (int i = 0; i < count; i++) {
				instance[i].delete();
				instance[i] = null;
			}

			count = 0;
		}

		private void updatePose(Matrix4f pose) {
			for (int i = 0; i < count; i++) {
				instance[i].setEffect(pose, x0[i], y0[i], width[i], height[i], depth[i]);
				instance[i].setChanged();
			}
		}

		public void add(float x0, float y0, float x1, float y1, float depth, GlyphInstance instance) {
			if (count >= capacity()) {
				reallocate(Math.max(2, capacity()) * 2);
			}

			this.x0[count] = x0;
			this.y0[count] = y0;
			this.width[count] = x1 - x0;
			this.height[count] = y1 - y0;
			this.depth[count] = depth;
			this.instance[count] = instance;

			count++;
		}

		private int capacity() {
			return x0.length;
		}

		private void reallocate(int capacity) {
			x0 = Arrays.copyOf(x0, capacity);
			y0 = Arrays.copyOf(y0, capacity);
			width = Arrays.copyOf(width, capacity);
			height = Arrays.copyOf(height, capacity);
			depth = Arrays.copyOf(depth, capacity);
			instance = Arrays.copyOf(instance, capacity);
		}

		private void shrinkToFit() {
			reallocate(count);
		}

		public void updateLight(int packedLight) {
			for (int i = 0; i < count; i++) {
				instance[i].light(packedLight);
				instance[i].setChanged();
			}
		}
	}

	/**
	 * Obfuscated glyphs. More complex than the other 2 SoAs.
	 */
	private static class ObfuscatedGlyphs {
		public static final IntList MISSING = IntList.of(-1);

		public int count = 0;

		// No need for RandomSource objects here, we can just do things locally.
		private long random = ThreadLocalRandom.current().nextLong();

		// One layer per layer. This array is sized separately from all others.
		public final TextLayer[] layers;

		// Per-glyph arrays. Lengths must be kept in sync.
		public float[] x = new float[0];
		public boolean[] italic = new boolean[0];
		public FontSet[] font = new FontSet[0];
		public IntList[] possibleCharacters = new IntList[0];
		public int[] possibleCharacterSizes = new int[0];
		public float[] boldOffset = new float[0];
		public float[] shadowOffset = new float[0];

		// Per-instance arrays. Length must be at least count * layers.size()
		public GlyphInstance[] instances = new GlyphInstance[0];

		private final Matrix4f cachedPose = new Matrix4f();

		private ObfuscatedGlyphs(List<TextLayer> layers) {
			this.layers = layers.toArray(new TextLayer[0]);
		}

		private int next() {
			// See SingleThreadedRandomSource#next
			long l;
			this.random = l = this.random * 25214903917L + 11L & 0xFFFFFFFFFFFFL;
			return (int)(l >> 48 - 31);
		}

		private int nextInt(int bound) {
			// This is what RandomSource#nextInt(bound) would do,
			// but I don't think we need such high quality randomness here.
			// int j;
			// int i;
			// if ((bound & bound - 1) == 0) {
			// 	return (int)((long)bound * (long)this.next() >> 31);
			// }
			// while ((i = this.next()) - (j = i % bound) + (bound - 1) < 0) {
			// }
			return next() % bound;
		}

		private int randomCharacter(int glyphIndex) {
			int size = this.possibleCharacterSizes[glyphIndex];

			return this.possibleCharacters[glyphIndex].getInt(nextInt(size));
		}

		public void add(InstancerProvider instancerProvider, float x, FontSet font, GlyphInfo glyphInfo, Style style) {
			if (count >= capacity()) {
				reallocate(Math.max(2, capacity()) * 2);
			}

			this.x[count] = x;
			this.font[count] = font;
			this.italic[count] = style.isItalic();

			// Replicate the logic from FontSet#getRandomGlyph
			this.possibleCharacters[count] = ((FontSetAccessor) font).vanillin$glyphsByWidth()
					.getOrDefault(Mth.ceil(glyphInfo.getAdvance(false)), MISSING);

			// Save this in an array too so we don't have to go through the indirection.
			this.possibleCharacterSizes[count] = this.possibleCharacters[count].size();

			BakedGlyph glyph = this.font[count].getGlyph(randomCharacter(count));

			var glyphExtension = TextUtil.getBakedGlyphExtension(glyph);
			float glyphWidth = glyphExtension.flywheel$right() - glyphExtension.flywheel$left();
			float glyphHeight = glyphExtension.flywheel$down() - glyphExtension.flywheel$up();

			ResourceLocation texture = glyphExtension.flywheel$texture();
			this.boldOffset[count] = style.isBold() ? glyphInfo.getBoldOffset() : 0.0f;
			this.shadowOffset[count] = glyphInfo.getShadowOffset();

			int instanceIndex = count * layers.length;
			for (TextLayer layer : layers) {
				var meshKeys = new GlyphMeshKey(glyphWidth, glyphHeight, layer.pattern(), this.boldOffset[count], this.shadowOffset[count]);
				var modelKey = new GlyphModelKey(meshKeys, layer.material(), texture);

				instances[instanceIndex] = instancerProvider.instancer(VanillinInstanceTypes.GLYPH, GLYPH_MODEL_CACHE.get(modelKey), layer.bias())
						.createInstance();
				instances[instanceIndex].colorArgb(layer.color()
						.color(style.getColor()));
				instanceIndex++;
			}

			count++;
		}

		public void updatePose(InstancerProvider instancerProvider, Matrix4f pose) {
			cachedPose.set(pose);

			update(instancerProvider);
		}

		public void update(InstancerProvider instancerProvider) {
			int instanceIndex = 0;
			for (int glyphIndex = 0; glyphIndex < count; glyphIndex++) {
				BakedGlyph glyph = font[glyphIndex].getGlyph(randomCharacter(glyphIndex));

				var glyphExtension = TextUtil.getBakedGlyphExtension(glyph);
				float left = glyphExtension.flywheel$left();
				float up = glyphExtension.flywheel$up();
				float glyphWidth = glyphExtension.flywheel$right() - left;
				float glyphHeight = glyphExtension.flywheel$down() - up;

				float boldOffset = this.boldOffset[glyphIndex];
				float shadowOffset = this.shadowOffset[glyphIndex];

				ResourceLocation texture = glyphExtension.flywheel$texture();
				for (TextLayer layer : layers) {
					Vector2fc offset = layer.offset();

					var meshKey = new GlyphMeshKey(glyphWidth, glyphHeight, layer.pattern(), boldOffset, shadowOffset);
					var modelKey = new GlyphModelKey(meshKey, layer.material(), texture);

					var instance = instances[instanceIndex];
					instancerProvider.instancer(VanillinInstanceTypes.GLYPH, GLYPH_MODEL_CACHE.get(modelKey), layer.bias())
							.stealInstance(instance);

					instance.setUvs(glyphExtension);
					instance.updatePose(cachedPose, x[glyphIndex] + offset.x() * shadowOffset, offset.y() * shadowOffset, left, up, italic[glyphIndex]);
					instance.setChanged();

					instanceIndex++;
				}
			}
		}

		private void updateLight(int light) {
			for (int i = 0; i < count * layers.length; i++) {
				instances[i].light(light);
				instances[i].setChanged();
			}
		}

		private int capacity() {
			return x.length;
		}

		private void reallocate(int capacity) {
			x = Arrays.copyOf(x, capacity);
			font = Arrays.copyOf(font, capacity);
			possibleCharacters = Arrays.copyOf(possibleCharacters, capacity);
			possibleCharacterSizes = Arrays.copyOf(possibleCharacterSizes, capacity);
			italic = Arrays.copyOf(italic, capacity);
			boldOffset = Arrays.copyOf(boldOffset, capacity);
			shadowOffset = Arrays.copyOf(shadowOffset, capacity);

			instances = Arrays.copyOf(instances, capacity * layers.length);
		}

		public void delete() {
			for (int i = 0; i < count * layers.length; i++) {
				instances[i].delete();
				instances[i] = null;
			}
			count = 0;
		}

		public void shrinkToFit() {
			reallocate(count);
		}
	}

	private static class Sink implements FormattedCharSink {
		private float x = 0;

		private final InstancerProvider instancerProvider;
		private final List<TextLayer> layers;

		@Nullable
		private BakedGlyphs glyphs;
		@Nullable
		private EffectGlyphs effectGlyphs;
		@Nullable
		private ObfuscatedGlyphs obfuscatedGlyphs;

		private Sink(InstancerProvider instancerProvider, List<TextLayer> layers) {
			this.instancerProvider = instancerProvider;
			this.layers = layers;
		}

		@Override
		public boolean accept(int index, Style style, int codePoint) {
			FontSet fontSet = TextUtil.getFontSet(FONT, style.getFont());
			GlyphInfo glyphInfo = fontSet.getGlyphInfo(codePoint, TextUtil.getFilterFishyGlyphs(FONT));

			boolean bold = style.isBold();
			float advance = glyphInfo.getAdvance(bold);

			// Write out obfuscated glyphs to a separate target that can better handle them.
			if (style.isObfuscated() && codePoint != ' ') {
				obfuscatedGlyphs().add(instancerProvider, x, fontSet, glyphInfo, style);
			} else {
				// Normal glyphs can be more thoroughly baked right here.
				BakedGlyph glyph = fontSet.getGlyph(codePoint);

				if (!(glyph instanceof EmptyGlyph)) {
					var glyphExtension = TextUtil.getBakedGlyphExtension(glyph);
					float glyphWidth = glyphExtension.flywheel$right() - glyphExtension.flywheel$left();
					float glyphHeight = glyphExtension.flywheel$down() - glyphExtension.flywheel$up();

					ResourceLocation texture = glyphExtension.flywheel$texture();
					float boldOffset = bold ? glyphInfo.getBoldOffset() : 0;
					float shadowOffset = glyphInfo.getShadowOffset();

					for (TextLayer layer : layers) {
						int color = layer.color()
								.color(style.getColor());
						Vector2fc offset = layer.offset();

						var meshKey = new GlyphMeshKey(glyphWidth, glyphHeight, layer.pattern(), boldOffset, shadowOffset);
						var modelKey = new GlyphModelKey(meshKey, layer.material(), texture);
						GlyphInstance instance = instancerProvider.instancer(VanillinInstanceTypes.GLYPH, GLYPH_MODEL_CACHE.get(modelKey), layer.bias())
								.createInstance();
						instance.colorArgb(color);
						instance.setUvs(glyphExtension);

						bakedGlyphs().add(x + offset.x() * shadowOffset, offset.y() * shadowOffset, glyphExtension.flywheel$left(), glyphExtension.flywheel$up(), style.isItalic(), instance);
					}
				}
			}

			// Now write out the strikethrough/underline glyphs.
			for (TextLayer layer : layers) {
				int color = layer.color()
						.color(style.getColor());
				Vector2fc offset = layer.offset();

				// SpecialGlyphs.WHITE, which effects use, has a shadowOffset of 1, so don't modify the offset returned by the layer.
				if (style.isStrikethrough()) {
					addEffect(layer, x + offset.x() - 1.0f, offset.y() + 4.5f, x + offset.x() + advance, offset.y() + 4.5f - 1.0f, 0.01f, color);
				}
				if (style.isUnderlined()) {
					addEffect(layer, x + offset.x() - 1.0f, offset.y() + 9.0f, x + offset.x() + advance, offset.y() + 9.0f - 1.0f, 0.01f, color);
				}
			}

			x += advance;
			return true;
		}

		private void addEffect(TextLayer layer, float x0, float y0, float x1, float y1, float depth, int colorArgb) {
			BakedGlyph glyph = TextUtil.getFontSet(FONT, Style.DEFAULT_FONT)
					.whiteGlyph();

			var glyphExtension = TextUtil.getBakedGlyphExtension(glyph);
			ResourceLocation texture = glyphExtension.flywheel$texture();
			TextLayer.GlyphMaterial material = layer.material();
			var modelKey = new GlyphModelKey(null, material, texture);

			GlyphInstance instance = instancerProvider.instancer(VanillinInstanceTypes.GLYPH, GLYPH_MODEL_CACHE.get(modelKey), layer.bias())
					.createInstance();
			instance.colorArgb(colorArgb);
			instance.setUvs(glyphExtension);

			effectGlyphs().add(x0, y0, x1, y1, depth, instance);
		}

		private ObfuscatedGlyphs obfuscatedGlyphs() {
			if (obfuscatedGlyphs == null) {
				obfuscatedGlyphs = new ObfuscatedGlyphs(layers);
			}
			return obfuscatedGlyphs;
		}

		private BakedGlyphs bakedGlyphs() {
			if (glyphs == null) {
				glyphs = new BakedGlyphs();
			}

			return glyphs;
		}

		private EffectGlyphs effectGlyphs() {
			if (effectGlyphs == null) {
				effectGlyphs = new EffectGlyphs();
			}

			return effectGlyphs;
		}
	}

	/**
	 * A mesh that represents a pattern of a glyph with a certain width and height. Expects to be drawn with the glyph
	 * instance type.
	 *
	 * @param offsets Each offset will be expanded into a glyph quad.
	 */
	private record GlyphMesh(float glyphWidth, float glyphHeight, Vector2fc[] offsets, Vector4fc boundingSphere) implements QuadMesh {
		private static final float[] X = new float[] { 0, 0, 1, 1 };
		private static final float[] Y = new float[] { 0, 1, 1, 0 };

		public GlyphMesh(float glyphWidth, float glyphHeight, Vector2fc[] offsets) {
			this(glyphWidth, glyphHeight, offsets, boundingSphere(glyphWidth, glyphHeight, offsets));
		}

		@Override
		public int vertexCount() {
			return 4 * offsets.length;
		}

		@Override
		public void write(MutableVertexList vertexList) {
			for (int i = 0; i < offsets.length; i++) {
				Vector2fc offset = offsets[i];
				var startVertex = i * 4;

				for (int j = 0; j < 4; j++) {
					vertexList.x(startVertex + j, offset.x() + (glyphWidth * X[j]));
					vertexList.y(startVertex + j, offset.y() + (glyphHeight * Y[j]));
					vertexList.z(startVertex + j, 0.0f);
					vertexList.r(startVertex + j, 1.0f);
					vertexList.g(startVertex + j, 1.0f);
					vertexList.b(startVertex + j, 1.0f);
					vertexList.a(startVertex + j, 1.0f);
					vertexList.u(startVertex + j, 0.0f);
					vertexList.v(startVertex + j, 0.0f);
					vertexList.overlay(startVertex + j, OverlayTexture.NO_OVERLAY);
					vertexList.light(startVertex + j, 0);
					vertexList.normalX(startVertex + j, 0.0f);
					vertexList.normalY(startVertex + j, 0.0f);
					vertexList.normalZ(startVertex + j, 1.0f);
				}
			}
		}

		@Override
		public Vector4fc boundingSphere() {
			return boundingSphere;
		}

		private static Vector4fc boundingSphere(float glyphWidth, float glyphHeight, Vector2fc[] offsets) {
			if (offsets.length == 0) {
				return new Vector4f(0, 0, 0, 0);
			}

			float minX = Float.POSITIVE_INFINITY;
			float minY = Float.POSITIVE_INFINITY;
			float maxX = Float.NEGATIVE_INFINITY;
			float maxY = Float.NEGATIVE_INFINITY;
			for (Vector2fc offset : offsets) {
				for (int j = 0; j < 4; j++) {
					var x = offset.x() + (glyphWidth * X[j]);
					var y = offset.y() + (glyphHeight * Y[j]);
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}

			float x = (minX + maxX) / 2;
			float y = (minY + maxY) / 2;

			float sizeX = maxX - minX;
			float sizeY = maxY - minY;
			float maxSize = Math.max(sizeX, sizeY);

			return new Vector4f(x, y, 0, Mth.SQRT_OF_TWO * maxSize / 2);
		}
	}

	private record GlyphEffectMesh() implements QuadMesh {
		private static final float[] X = new float[] { 0, 1, 1, 0 };
		private static final float[] Y = new float[] { 0, 0, 1, 1 };
		private static final Vector4fc BOUNDING_SPHERE = new Vector4f(0.5f, 0.5f, 0, Mth.SQRT_OF_TWO * 0.5f);

		public static final GlyphEffectMesh INSTANCE = new GlyphEffectMesh();

		@Override
		public int vertexCount() {
			return 4;
		}

		@Override
		public void write(MutableVertexList vertexList) {
			for (int i = 0; i < 4; i++) {
				vertexList.x(i, X[i]);
				vertexList.y(i, Y[i]);
				vertexList.z(i, 0.0f);
				vertexList.r(i, 1.0f);
				vertexList.g(i, 1.0f);
				vertexList.b(i, 1.0f);
				vertexList.a(i, 1.0f);
				vertexList.u(i, 0.0f);
				vertexList.v(i, 0.0f);
				vertexList.overlay(i, OverlayTexture.NO_OVERLAY);
				vertexList.light(i, 0);
				vertexList.normalX(i, 0.0f);
				vertexList.normalY(i, 0.0f);
				vertexList.normalZ(i, 1.0f);
			}
		}

		@Override
		public Vector4fc boundingSphere() {
			return BOUNDING_SPHERE;
		}
	}
}
