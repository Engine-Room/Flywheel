package dev.engine_room.flywheel.lib.visual;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.mojang.blaze3d.font.GlyphInfo;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.lib.instance.GlyphInstance;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
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
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * A visual that renders a single line of text.
 */
public class TextVisual {
	public static final float ONE_PIXEL = 0.125f;

	public boolean dropShadow;
	public boolean with8xOutline;
	public int backgroundColor = 0;
	public int color;
	public FormattedCharSequence content = FormattedCharSequence.EMPTY;
	public float x;
	public float y;
	public int light;

	public final Matrix4f pose = new Matrix4f();

	private final Sink sink;

	public TextVisual(InstancerProvider provider) {
		sink = new Sink(provider);
	}

	public void setup() {
		sink.recycler.resetCount();
		sink.x = x;
		sink.y = y;
		sink.dimFactor = dropShadow ? 0.25f : 1.0f;
		sink.r = (float) (color >> 16 & 0xFF) / 255.0f * sink.dimFactor;
		sink.g = (float) (color >> 8 & 0xFF) / 255.0f * sink.dimFactor;
		sink.b = (float) (color & 0xFF) / 255.0f * sink.dimFactor;
		sink.a = (float) (color >> 24 & 0xFF) / 255.0f;
		// FIXME: Need separate instances for the 8x outline and the center.
		//  Right now we just show the outline.
		content.accept(sink);
		sink.recycler.discardExtra();
	}

	public void delete() {
		sink.recycler.delete();
	}

	private class Sink implements FormattedCharSink {
		private final SmartRecycler<GlyphModelKey, GlyphInstance> recycler;

		Font font;
		private float dimFactor;
		private float r;
		private float g;
		private float b;
		private float a;

		// Separate x and y from TextVisual because these advance as we accept glyphs
		float x;
		float y;

		private Sink(InstancerProvider instancerProvider) {
			recycler = new SmartRecycler<>(key -> instancerProvider.instancer(InstanceTypes.GLYPH, GLYPH_CACHE.get(key))
					.createInstance());
			font = Minecraft.getInstance().font;
		}

		@Override
		public boolean accept(int i, Style style, int j) {
			float b;
			float g;
			float r;
			FontSet fontSet = FlwLibLink.INSTANCE.getFontSet(font, style.getFont());
			GlyphInfo glyphInfo = fontSet.getGlyphInfo(j, false);
			BakedGlyph bakedGlyph = style.isObfuscated() && j != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(j);
			boolean bold = style.isBold();
			TextColor textColor = style.getColor();
			if (textColor != null) {
				int color = textColor.getValue();
				r = (float) (color >> 16 & 0xFF) / 255.0f * this.dimFactor;
				g = (float) (color >> 8 & 0xFF) / 255.0f * this.dimFactor;
				b = (float) (color & 0xFF) / 255.0f * this.dimFactor;
			} else {
				r = this.r;
				g = this.g;
				b = this.b;
			}
			if (!(bakedGlyph instanceof EmptyGlyph)) {
				var glyphExtension = FlwLibLink.INSTANCE.getGlyphExtension(bakedGlyph);

				GlyphInstance glyph = recycler.get(new GlyphModelKey(glyphExtension.flywheel$texture(), new GlyphSettings(bold, dropShadow, with8xOutline)));

				glyph.pose.set(pose);
				glyph.setGlyph(bakedGlyph, this.x, this.y, style.isItalic());
				glyph.color(r, g, b, this.a);
				glyph.light = light;
				glyph.setChanged();
			}
			float advance = glyphInfo.getAdvance(bold);
			float o = dropShadow ? 1.0f : 0.0f;
			if (style.isStrikethrough()) {
				this.addEffect(this.x + o - 1.0f, this.y + o + 4.5f, this.x + o + advance, this.y + o + 4.5f - 1.0f, 0.01f, r, g, b, this.a);
			}
			if (style.isUnderlined()) {
				this.addEffect(this.x + o - 1.0f, this.y + o + 9.0f, this.x + o + advance, this.y + o + 9.0f - 1.0f, 0.01f, r, g, b, this.a);
			}
			this.x += advance;
			return true;
		}

		private void addEffect(float x0, float y0, float x1, float y1, float depth, float r, float g, float b, float a) {
			BakedGlyph bakedGlyph = FlwLibLink.INSTANCE.getFontSet(font, Style.DEFAULT_FONT)
					.whiteGlyph();

			var glyphExtension = FlwLibLink.INSTANCE.getGlyphExtension(bakedGlyph);

			GlyphInstance glyph = recycler.get(new GlyphModelKey(glyphExtension.flywheel$texture(), new GlyphSettings(false, dropShadow, with8xOutline)));

			glyph.pose.set(pose);
			glyph.setEffect(bakedGlyph, x0, y0, x1, y1, depth);
			glyph.color(r, g, b, this.a);
			glyph.light = light;
			glyph.setChanged();
		}

		public float finish(int backgroundColor, float x) {
			if (backgroundColor != 0) {
				float f = (float) (backgroundColor >> 24 & 0xFF) / 255.0f;
				float g = (float) (backgroundColor >> 16 & 0xFF) / 255.0f;
				float h = (float) (backgroundColor >> 8 & 0xFF) / 255.0f;
				float i = (float) (backgroundColor & 0xFF) / 255.0f;
				this.addEffect(x - 1.0f, this.y + 9.0f, this.x + 1.0f, this.y - 1.0f, 0.01f, g, h, i, f);
			}
			return this.x;
		}
	}

	private static final ResourceReloadCache<GlyphModelKey, Model> GLYPH_CACHE = new ResourceReloadCache<>(GlyphModelKey::into);
	private static final ResourceReloadCache<GlyphSettings, GlyphMesh> MESH_CACHE = new ResourceReloadCache<>(GlyphSettings::into);

	private static final Material GLYPH_MATERIAL = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.build();

	private record GlyphModelKey(ResourceLocation font, GlyphSettings settings) {
		private Model into() {
			return new SingleMeshModel(MESH_CACHE.get(settings), SimpleMaterial.builderOf(GLYPH_MATERIAL)
					.texture(font)
					.build());
		}
	}

	// FIXME: probably replace with an enum
	private record GlyphSettings(boolean bold, boolean dropShadow, boolean with8xOutline) {
		public GlyphMesh into() {
			// bold -> x + 1
			// shadow -> x + 1, y + 1

			List<Vector3f> out = new ArrayList<>();

			if (with8xOutline) {
				for (int x = -1; x <= 1; ++x) {
					for (int y = -1; y <= 1; ++y) {
						if (x == 0 && y == 0) {
							continue;
						}

						out.add(new Vector3f(x * ONE_PIXEL, y * ONE_PIXEL, 0));
					}
				}
			} else {
				out.add(new Vector3f(0, 0, 0));
			}

			if (bold) {
				out.add(new Vector3f(ONE_PIXEL, 0, 0));
			}

			if (dropShadow) {
				out.add(new Vector3f(ONE_PIXEL, ONE_PIXEL, 0));
			}

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
