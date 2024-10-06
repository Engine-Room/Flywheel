package dev.engine_room.flywheel.lib.visual.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
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
import dev.engine_room.flywheel.lib.instance.GlyphInstance;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import dev.engine_room.flywheel.lib.model.QuadMesh;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.util.ResourceReloadCache;
import dev.engine_room.flywheel.lib.visual.util.SmartRecycler;
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

	private static final ResourceReloadCache<GlyphMeshKey, GlyphMesh> GLYPH_MESH_CACHE = new ResourceReloadCache<>(GlyphMeshKey::into);
	private static final ResourceReloadCache<GlyphModelKey, Model> GLYPH_MODEL_CACHE = new ResourceReloadCache<>(GlyphModelKey::into);

	private static final ThreadLocal<Sink> SINKS = ThreadLocal.withInitial(Sink::new);

	private final SmartRecycler<GlyphInstanceKey, GlyphInstance> recycler;
	private final List<TextLayer> layers = new ArrayList<>();
	private final Matrix4f pose = new Matrix4f();

	private FormattedCharSequence text = FormattedCharSequence.EMPTY;
	private float x;
	private float y;
	private int backgroundColor = 0;
	private int light;

	public TextVisual(InstancerProvider provider) {
		recycler = new SmartRecycler<>(key -> provider.instancer(InstanceTypes.GLYPH, GLYPH_MODEL_CACHE.get(key.modelKey), key.bias)
				.createInstance());
	}

	public TextVisual addLayer(TextLayer layer) {
		layers.add(layer);
		return this;
	}

	public TextVisual addLayers(Collection<TextLayer> layers) {
		this.layers.addAll(layers);
		return this;
	}

	public TextVisual layers(Collection<TextLayer> layers) {
		this.layers.clear();
		this.layers.addAll(layers);
		return this;
	}

	public TextVisual clearLayers() {
		layers.clear();
		return this;
	}

	public Matrix4f pose() {
		return pose;
	}

	public TextVisual text(FormattedCharSequence text) {
		this.text = text;
		return this;
	}

	public TextVisual x(float x) {
		this.x = x;
		return this;
	}

	public TextVisual y(float y) {
		this.y = y;
		return this;
	}

	public TextVisual pos(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public TextVisual backgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public TextVisual light(int light) {
		this.light = light;
		return this;
	}

	public TextVisual reset() {
		layers.clear();
		pose.identity();

		text = FormattedCharSequence.EMPTY;
		x = 0;
		y = 0;
		backgroundColor = 0;
		light = 0;

		return this;
	}

	// TODO: track glyph instances and add method to update only UVs of obfuscated glyphs, method to update only
	//  background color, and method to only update light
	public void setup() {
		recycler.resetCount();

		var sink = SINKS.get();
		sink.prepare(recycler, pose, light);

		int maxX = 0;
		for (TextLayer layer : layers) {
			sink.prepareForLayer(layer, x, y);
			text.accept(sink);
			maxX = Math.max(maxX, (int) sink.x);
		}

		sink.addBackground(backgroundColor, x, maxX);
		sink.clear();

		recycler.discardExtra();
	}

	public void delete() {
		recycler.delete();
	}

	private record GlyphMeshKey(float glyphWidth, float glyphHeight, TextLayer.GlyphPattern pattern, boolean bold, float boldOffset, float shadowOffset) {
		public GlyphMesh into() {
			List<Vector2fc> out = new ArrayList<>();

			pattern.addGlyphs(offsetc -> {
				Vector2f offset = new Vector2f(offsetc).mul(shadowOffset);
				out.add(offset);

				if (bold) {
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

	private record GlyphInstanceKey(GlyphModelKey modelKey, int bias) {
	}

	private static class Sink implements FormattedCharSink {
		@UnknownNullability
		private SmartRecycler<GlyphInstanceKey, GlyphInstance> recycler;
		@UnknownNullability
		private Matrix4f pose;
		private int light;

		@UnknownNullability
		private TextLayer layer;
		private float x;
		private float y;

		public void prepare(SmartRecycler<GlyphInstanceKey, GlyphInstance> recycler, Matrix4f pose, int light) {
			this.recycler = recycler;
			this.pose = pose;
			this.light = light;
		}

		public void prepareForLayer(TextLayer layer, float x, float y) {
			this.layer = layer;
			this.x = x;
			this.y = y;
		}

		public void clear() {
			recycler = null;
			pose = null;
			layer = null;
		}

		@Override
		public boolean accept(int index, Style style, int codePoint) {
			FontSet fontSet = FlwLibLink.INSTANCE.getFontSet(FONT, style.getFont());
			GlyphInfo glyphInfo = fontSet.getGlyphInfo(codePoint, FlwLibLink.INSTANCE.getFilterFishyGlyphs(FONT));
			BakedGlyph glyph = style.isObfuscated() && codePoint != ' ' ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(codePoint);

			boolean bold = style.isBold();
			int color = layer.color()
					.color(style.getColor());
			Vector2fc offset = layer.offset();

			if (!(glyph instanceof EmptyGlyph)) {
				GlyphInstance instance = recycler.get(key(glyphInfo, glyph, layer.pattern(), bold));
				float shadowOffset = glyphInfo.getShadowOffset();
				instance.setGlyph(glyph, pose, x + offset.x() * shadowOffset, y + offset.y() * shadowOffset, style.isItalic());
				instance.colorArgb(color);
				instance.light(light);
				instance.setChanged();
			}

			float advance = glyphInfo.getAdvance(bold);
			// SpecialGlyphs.WHITE, which effects use, has a shadowOffset of 1, so don't modify the offset returned by the layer.
			float effectOffsetX = offset.x();
			float effectOffsetY = offset.y();
			if (style.isStrikethrough()) {
				addEffect(x + effectOffsetX - 1.0f, y + effectOffsetY + 4.5f, x + effectOffsetX + advance, y + effectOffsetY + 4.5f - 1.0f, 0.01f, color);
			}
			if (style.isUnderlined()) {
				addEffect(x + effectOffsetX - 1.0f, y + effectOffsetY + 9.0f, x + effectOffsetX + advance, y + effectOffsetY + 9.0f - 1.0f, 0.01f, color);
			}

			x += advance;
			return true;
		}

		public void addBackground(int backgroundColor, float startX, float endX) {
			if (backgroundColor != 0) {
				addEffect(startX - 1.0f, y + 9.0f, endX + 1.0f, y - 1.0f, 0.01f, backgroundColor);
			}
		}

		private void addEffect(float x0, float y0, float x1, float y1, float depth, int colorArgb) {
			BakedGlyph glyph = FlwLibLink.INSTANCE.getFontSet(FONT, Style.DEFAULT_FONT)
					.whiteGlyph();

			GlyphInstance instance = recycler.get(effectKey(glyph));
			instance.setEffect(glyph, pose, x0, y0, x1, y1, depth);
			instance.colorArgb(colorArgb);
			instance.light(light);
			instance.setChanged();
		}

		private GlyphInstanceKey key(GlyphInfo glyphInfo, BakedGlyph glyph, TextLayer.GlyphPattern pattern, boolean bold) {
			var glyphExtension = FlwLibLink.INSTANCE.getBakedGlyphExtension(glyph);
			float glyphWidth = glyphExtension.flywheel$right() - glyphExtension.flywheel$left();
			float glyphHeight = glyphExtension.flywheel$down() - glyphExtension.flywheel$up();

			return key(glyphWidth, glyphHeight, glyphExtension.flywheel$texture(), pattern, bold, bold ? glyphInfo.getBoldOffset() : 0, glyphInfo.getShadowOffset());
		}

		private GlyphInstanceKey key(float glyphWidth, float glyphHeight, ResourceLocation texture, TextLayer.GlyphPattern pattern, boolean bold, float boldOffset, float shadowOffset) {
			var meshKey = new GlyphMeshKey(glyphWidth, glyphHeight, pattern, bold, boldOffset, shadowOffset);
			var modelKey = new GlyphModelKey(meshKey, layer.material(), texture);
			return new GlyphInstanceKey(modelKey, layer.bias());
		}

		private GlyphInstanceKey effectKey(BakedGlyph glyph) {
			var glyphExtension = FlwLibLink.INSTANCE.getBakedGlyphExtension(glyph);
			return effectKey(glyphExtension.flywheel$texture());
		}

		private GlyphInstanceKey effectKey(ResourceLocation texture) {
			var modelKey = new GlyphModelKey(null, layer.material(), texture);
			return new GlyphInstanceKey(modelKey, layer.bias());
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
					vertexList.z(startVertex + j, 0);
					vertexList.r(startVertex + j, 1);
					vertexList.g(startVertex + j, 1);
					vertexList.b(startVertex + j, 1);
					vertexList.a(startVertex + j, 1);
					vertexList.overlay(startVertex + j, OverlayTexture.NO_OVERLAY);
					vertexList.normalX(startVertex + j, 0);
					vertexList.normalY(startVertex + j, 0);
					vertexList.normalZ(startVertex + j, 1);
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
				vertexList.z(i, 0);
				vertexList.r(i, 1);
				vertexList.g(i, 1);
				vertexList.b(i, 1);
				vertexList.a(i, 1);
				vertexList.normalX(i, 0);
				vertexList.normalY(i, 0);
				vertexList.normalZ(i, 1);
			}
		}

		@Override
		public Vector4fc boundingSphere() {
			return BOUNDING_SPHERE;
		}
	}
}
