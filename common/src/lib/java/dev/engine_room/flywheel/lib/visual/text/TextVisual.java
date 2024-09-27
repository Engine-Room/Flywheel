package dev.engine_room.flywheel.lib.visual.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.mojang.blaze3d.font.GlyphInfo;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * A visual that renders a single line of text.
 */
public class TextVisual {
	private static final ThreadLocal<Sink> SINKS = ThreadLocal.withInitial(Sink::new);

	private final SmartRecycler<GlyphInstancerKey, GlyphInstance> recycler;

	private FormattedCharSequence content = FormattedCharSequence.EMPTY;
	private float x;
	private float y;
	private int backgroundColor = 0;
	private int light;
	private boolean fullBright;

	private final List<TextLayer> layers = new ArrayList<>();

	private final Matrix4f pose = new Matrix4f();

	public TextVisual(InstancerProvider provider) {
		recycler = new SmartRecycler<>(key -> provider.instancer(InstanceTypes.GLYPH, GLYPH_CACHE.get(key.modelKey), key.bias)
				.createInstance());
	}

	public TextVisual content(FormattedCharSequence content) {
		this.content = content;
		return this;
	}

	public Matrix4f pose() {
		return pose;
	}

	public TextVisual clearLayers() {
		layers.clear();
		return this;
	}

	public TextVisual addLayer(TextLayer layer) {
		layers.add(layer);
		return this;
	}

	public TextVisual layers(Collection<TextLayer> layers) {
		this.layers.clear();
		this.layers.addAll(layers);
		return this;
	}

	public TextVisual pos(float x, float y) {
		this.x = x;
		this.y = y;
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

	public TextVisual backgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public TextVisual light(int light) {
		this.light = light;
		return this;
	}

	public TextVisual fullBright(boolean fullBright) {
		this.fullBright = fullBright;
		return this;
	}

	// TODO: method to just update pose or light without recalculating text
	public void setup() {
		recycler.resetCount();

		var sink = SINKS.get();

		var light = fullBright ? LightTexture.FULL_BRIGHT : this.light;
		sink.prepare(recycler, pose, light);

		int maxX = 0;
		// Can we flip the inner and outer loops here?
		// Would that even be better?
		for (TextLayer layer : layers) {
			sink.x = x;
			sink.y = y;
			sink.layer = layer;
			content.accept(sink);
			maxX = Math.max(maxX, (int) sink.x);
		}

		sink.addBackground(backgroundColor, x, maxX);

		sink.clear();

		recycler.discardExtra();
	}

	public void delete() {
		recycler.delete();
	}

	private static class Sink implements FormattedCharSink {
		private final Font font;

		@UnknownNullability
		private SmartRecycler<GlyphInstancerKey, GlyphInstance> recycler;
		@UnknownNullability
		private Matrix4f pose;
		@UnknownNullability
		private TextLayer layer;

		private int light;

		private float x;
		private float y;

		private Sink() {
			font = Minecraft.getInstance().font;
		}

		private void prepare(SmartRecycler<GlyphInstancerKey, GlyphInstance> recycler, Matrix4f pose, int light) {
			this.recycler = recycler;
			this.pose = pose;
			this.light = light;
		}

		private void clear() {
			recycler = null;
			pose = null;
			layer = null;
		}

		@Override
		public boolean accept(int i, Style style, int j) {
			FontSet fontSet = FlwLibLink.INSTANCE.getFontSet(font, style.getFont());
			GlyphInfo glyphInfo = fontSet.getGlyphInfo(j, false);
			BakedGlyph bakedGlyph = style.isObfuscated() && j != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(j);
			boolean bold = style.isBold();

			int color = layer.color()
					.color(style);

			if (!(bakedGlyph instanceof EmptyGlyph)) {
				var glyphExtension = FlwLibLink.INSTANCE.getGlyphExtension(bakedGlyph);

				GlyphInstance glyph = recycler.get(key(glyphExtension.flywheel$texture(), bold, layer.style()));

				glyph.pose.set(pose);
				glyph.setGlyph(bakedGlyph, this.x + layer.offsetX(), this.y + layer.offsetY(), style.isItalic());
				glyph.colorArgb(color);
				glyph.light = light;
				glyph.setChanged();
			}
			float advance = glyphInfo.getAdvance(bold);
			float effectX = layer.effectOffsetX();
			float effectY = layer.effectOffsetY();
			if (style.isStrikethrough()) {
				this.addEffect(this.x + effectX - 1.0f, this.y + effectY + 4.5f, this.x + effectX + advance, this.y + effectY + 4.5f - 1.0f, 0.01f, color);
			}
			if (style.isUnderlined()) {
				this.addEffect(this.x + effectX - 1.0f, this.y + effectY + 9.0f, this.x + effectX + advance, this.y + effectY + 9.0f - 1.0f, 0.01f, color);
			}
			this.x += advance;
			return true;
		}

		private void addEffect(float x0, float y0, float x1, float y1, float depth, int colorArgb) {
			BakedGlyph bakedGlyph = FlwLibLink.INSTANCE.getFontSet(font, Style.DEFAULT_FONT)
					.whiteGlyph();

			var glyphExtension = FlwLibLink.INSTANCE.getGlyphExtension(bakedGlyph);

			GlyphInstance glyph = recycler.get(key(glyphExtension.flywheel$texture(), false, TextLayer.GlyphMeshStyle.SIMPLE));

			glyph.pose.set(pose);
			glyph.setEffect(bakedGlyph, x0, y0, x1, y1, depth);
			glyph.colorArgb(colorArgb);
			glyph.light = light;
			glyph.setChanged();
		}

		public void addBackground(int backgroundColor, float startX, float endX) {
			if (backgroundColor != 0) {
				this.addEffect(startX - 1.0f, this.y + 9.0f, endX + 1.0f, this.y - 1.0f, 0.01f, backgroundColor);
			}
		}

		private GlyphInstancerKey key(ResourceLocation texture, boolean bold, TextLayer.GlyphMeshStyle style) {
			var meshKey = new GlyphMeshKey(style, bold);
			var modelKey = new GlyphModelKey(texture, meshKey, layer.material());
			return new GlyphInstancerKey(modelKey, layer.bias());
		}
	}

	private record GlyphInstancerKey(GlyphModelKey modelKey, int bias) {
	}

	private static final ResourceReloadCache<GlyphModelKey, Model> GLYPH_CACHE = new ResourceReloadCache<>(GlyphModelKey::into);
	private static final ResourceReloadCache<GlyphMeshKey, GlyphMesh> MESH_CACHE = new ResourceReloadCache<>(GlyphMeshKey::into);

	private record GlyphModelKey(ResourceLocation font, GlyphMeshKey meshKey, TextLayer.GlyphMaterial material) {
		private Model into() {
			return new SingleMeshModel(MESH_CACHE.get(meshKey), material.create(font));
		}
	}

	private record GlyphMeshKey(TextLayer.GlyphMeshStyle style, boolean bold) {
		public GlyphMesh into() {
			List<Vector3f> out = new ArrayList<>();

			style.addQuads(quad -> {
				out.add(quad);
				if (bold) {
					out.add(new Vector3f(quad.x + TextLayer.ONE_PIXEL, quad.y, quad.z));
				}
			});

			return new GlyphMesh(out.toArray(new Vector3f[0]));
		}
	}

	/**
	 * A mesh that represents a single glyph. Expects to be drawn with the glyph instance type.
	 *
	 * @param quads Each quad will be expanded into 4 vertices.
	 */
	private record GlyphMesh(Vector3f[] quads) implements QuadMesh {
		private static final float[] X = new float[]{0, 0, 1, 1};
		private static final float[] Y = new float[]{0, 1, 1, 0};

		@Override
		public int vertexCount() {
			return 4 * quads.length;
		}

		@Override
		public void write(MutableVertexList vertexList) {
			for (int i = 0; i < quads.length; i++) {
				Vector3f quad = quads[i];
				var quadStart = i * 4;

				for (int j = 0; j < 4; j++) {
					vertexList.x(quadStart + j, quad.x + X[j]);
					vertexList.y(quadStart + j, quad.y + Y[j]);
					vertexList.z(quadStart + j, quad.z);
					vertexList.normalX(quadStart + j, 0);
					vertexList.normalY(quadStart + j, 0);
					vertexList.normalZ(quadStart + j, 1);
					vertexList.overlay(quadStart + j, OverlayTexture.NO_OVERLAY);
					vertexList.r(quadStart + j, 1);
					vertexList.g(quadStart + j, 1);
					vertexList.b(quadStart + j, 1);
					vertexList.a(quadStart + j, 1);
				}
			}
		}

		@Override
		public Vector4fc boundingSphere() {
			// FIXME: what is the actual bounding sphere??
			return new Vector4f(0, 0, 0, 2);
		}
	}
}
