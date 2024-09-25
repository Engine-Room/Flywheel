package dev.engine_room.flywheel.lib.visual;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.google.common.collect.Lists;
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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSink;

public class NameplateVisual {

	public final Matrix4f pose = new Matrix4f();
	private final InstancerProvider provider;
	private final InstanceEmitter instanceEmitter;

	@Nullable
	private Component name;

	public NameplateVisual(InstancerProvider provider) {
		this.provider = provider;

		instanceEmitter = new InstanceEmitter(provider);
		instanceEmitter.font = Minecraft.getInstance().font;
	}

	public void name(Component name) {
		this.name = name;
	}

	public void update() {
		if (name == null) {
			return;
		}

		instanceEmitter.recycler.resetCount();
		instanceEmitter.x = 0;
		instanceEmitter.y = 0;
		name.getVisualOrderText()
				.accept(instanceEmitter);
		instanceEmitter.recycler.discardExtra();

	}

	private class InstanceEmitter implements FormattedCharSink {
		private final SmartRecycler<GlyphModelKey, GlyphInstance> recycler;

		Font font;
		private boolean dropShadow;
		private float dimFactor;
		private byte r;
		private byte g;
		private byte b;
		private byte a;
		private Font.DisplayMode mode;
		private int packedLightCoords;
		float x;
		float y;

		private InstanceEmitter(InstancerProvider instancerProvider) {
			recycler = new SmartRecycler<>(key -> instancerProvider.instancer(InstanceTypes.GLYPH, GLYPH_CACHE.get(key))
					.createInstance());
		}

		@Nullable
		private List<BakedGlyph.Effect> effects;

		private void addEffect(BakedGlyph.Effect effect) {
			if (this.effects == null) {
				this.effects = Lists.newArrayList();
			}
			this.effects.add(effect);
		}

		@Override
		public boolean accept(int i, Style style, int j) {
			float n;
			float l;
			float h;
			float g;
			FontSet fontSet = FlwLibLink.INSTANCE.getFontSet(font, style.getFont());
			GlyphInfo glyphInfo = fontSet.getGlyphInfo(j, false);
			BakedGlyph bakedGlyph = style.isObfuscated() && j != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(j);
			boolean bl = style.isBold();
			float f = this.a;
			TextColor textColor = style.getColor();
			if (textColor != null) {
				int k = textColor.getValue();
				g = (float) (k >> 16 & 0xFF) / 255.0f * this.dimFactor;
				h = (float) (k >> 8 & 0xFF) / 255.0f * this.dimFactor;
				l = (float) (k & 0xFF) / 255.0f * this.dimFactor;
			} else {
				g = this.r;
				h = this.g;
				l = this.b;
			}
			if (!(bakedGlyph instanceof EmptyGlyph)) {
				// var renderType = bakedGlyph.renderType(Font.DisplayMode.NORMAL);

				var glyphExtension = FlwLibLink.INSTANCE.getGlyphExtension(bakedGlyph);

				GlyphInstance glyph = recycler.get(new GlyphModelKey(glyphExtension.flywheel$texture(), bl, dropShadow));

				glyph.setGlyph(bakedGlyph, this.x, this.y, style.isItalic());
				glyph.pose.set(pose);
				glyph.light = LightTexture.FULL_BRIGHT;
				glyph.setChanged();
			}
			float m = glyphInfo.getAdvance(bl);
			float f2 = n = this.dropShadow ? 1.0f : 0.0f;
			//			if (style.isStrikethrough()) {
			//				this.addEffect(new BakedGlyph.Effect(this.x + n - 1.0f, this.y + n + 4.5f, this.x + n + m, this.y + n + 4.5f - 1.0f, 0.01f, g, h, l, f));
			//			}
			//			if (style.isUnderlined()) {
			//				this.addEffect(new BakedGlyph.Effect(this.x + n - 1.0f, this.y + n + 9.0f, this.x + n + m, this.y + n + 9.0f - 1.0f, 0.01f, g, h, l, f));
			//			}
			this.x += m;
			return true;
		}

		public float finish(int backgroundColor, float x) {
			if (backgroundColor != 0) {
				float f = (float) (backgroundColor >> 24 & 0xFF) / 255.0f;
				float g = (float) (backgroundColor >> 16 & 0xFF) / 255.0f;
				float h = (float) (backgroundColor >> 8 & 0xFF) / 255.0f;
				float i = (float) (backgroundColor & 0xFF) / 255.0f;
				this.addEffect(new BakedGlyph.Effect(x - 1.0f, this.y + 9.0f, this.x + 1.0f, this.y - 1.0f, 0.01f, g, h, i, f));
			}
			//			if (this.effects != null) {
			//				BakedGlyph bakedGlyph = font.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
			//				VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));
			//				for (BakedGlyph.Effect effect : this.effects) {
			//					bakedGlyph.renderEffect(effect, this.pose, vertexConsumer, this.packedLightCoords);
			//				}
			//			}
			return this.x;
		}
	}

	private static final ResourceReloadCache<GlyphModelKey, Model> GLYPH_CACHE = new ResourceReloadCache<>(GlyphModelKey::into);

	private static final Material GLYPH_MATERIAL = SimpleMaterial.builder()
			.build();

	private record GlyphModelKey(ResourceLocation font, boolean bold, boolean shadow) {
		private Model into() {
			// bold -> x + 1
			// shadow -> x + 1, y + 1
			return new SingleMeshModel(new GlyphMesh(new Vector3f[]{new Vector3f(0, 0, 0),}), SimpleMaterial.builderOf(GLYPH_MATERIAL)
					.texture(font)
					.cutout(CutoutShaders.ONE_TENTH)
					.build());
		}
	}

	public record GlyphMesh(Vector3f[] quads) implements QuadMesh {

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
					vertexList.x(quadStart + j, quad.x);
					vertexList.y(quadStart + j, quad.y);
					vertexList.z(quadStart + j, quad.z);
					vertexList.normalX(quadStart + j, 0);
					vertexList.normalY(quadStart + j, 0);
					vertexList.normalZ(quadStart + j, 1);
				}
			}
		}

		@Override
		public Vector4fc boundingSphere() {
			return new Vector4f(0, 0, 0, 2);
		}
	}
}
