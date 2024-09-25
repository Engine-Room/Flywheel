package dev.engine_room.flywheel.backend.font;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.lib.internal.GlyphExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class AsyncFontTexture extends AbstractTexture implements Dumpable {
	private static final int SIZE = 256;
	private final GlyphRenderTypes renderTypes;
	private final ResourceLocation name;
	private final boolean colored;
	private final Node root;

	private final List<Upload> uploads = Lists.newArrayList();

	private boolean flushScheduled = false;

	public AsyncFontTexture(ResourceLocation name, GlyphRenderTypes renderTypes, boolean colored) {
		this.name = name;
		this.colored = colored;
		this.root = new Node(0, 0, 256, 256);
		this.renderTypes = renderTypes;

		if (RenderSystem.isOnRenderThreadOrInit()) {
			this.init();
		} else {
			RenderSystem.recordRenderCall(this::init);
		}
	}

	@Override
	public void load(ResourceManager resourceManager) {
	}

	@Override
	public void close() {
		this.releaseId();
	}

	@Nullable
	public BakedGlyph add(SheetGlyphInfo glyphInfo) {
		if (glyphInfo.isColored() != this.colored) {
			return null;
		}
		Node node = this.root.insert(glyphInfo);
		if (node != null) {
			if (RenderSystem.isOnRenderThreadOrInit()) {
				this.bind();
				glyphInfo.upload(node.x, node.y);
			} else {
				uploads.add(new Upload(glyphInfo, node.x, node.y));

				if (!flushScheduled) {
					RenderSystem.recordRenderCall(this::flush);
					flushScheduled = true;
				}
			}
			var out = new BakedGlyph(this.renderTypes, ((float) node.x + 0.01f) / 256.0f, ((float) node.x - 0.01f + (float) glyphInfo.getPixelWidth()) / 256.0f, ((float) node.y + 0.01f) / 256.0f, ((float) node.y - 0.01f + (float) glyphInfo.getPixelHeight()) / 256.0f, glyphInfo.getLeft(), glyphInfo.getRight(), glyphInfo.getUp(), glyphInfo.getDown());

			((GlyphExtension) out).flywheel$texture(name);

			return out;
		}
		return null;
	}

	@Override
	public void dumpContents(ResourceLocation resourceLocation, Path path) {
		String string = resourceLocation.toDebugFileName();
		TextureUtil.writeAsPNG(path, string, this.getId(), 0, 256, 256, i -> (i & 0xFF000000) == 0 ? -16777216 : i);
	}

	public void init() {
		TextureUtil.prepareImage(colored ? NativeImage.InternalGlFormat.RGBA : NativeImage.InternalGlFormat.RED, this.getId(), 256, 256);
	}

	public void flush() {
		this.bind();
		for (Upload upload : this.uploads) {
			upload.info.upload(upload.x, upload.y);
		}

		uploads.clear();

		flushScheduled = false;
	}

	public record Upload(SheetGlyphInfo info, int x, int y) {
	}

	@Environment(value = EnvType.CLIENT)
	static class Node {
		final int x;
		final int y;
		private final int width;
		private final int height;
		@Nullable
		private Node left;
		@Nullable
		private Node right;
		private boolean occupied;

		Node(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		@Nullable Node insert(SheetGlyphInfo glyphInfo) {
			if (this.left != null && this.right != null) {
				Node node = this.left.insert(glyphInfo);
				if (node == null) {
					node = this.right.insert(glyphInfo);
				}
				return node;
			}
			if (this.occupied) {
				return null;
			}
			int i = glyphInfo.getPixelWidth();
			int j = glyphInfo.getPixelHeight();
			if (i > this.width || j > this.height) {
				return null;
			}
			if (i == this.width && j == this.height) {
				this.occupied = true;
				return this;
			}
			int k = this.width - i;
			int l = this.height - j;
			if (k > l) {
				this.left = new Node(this.x, this.y, i, this.height);
				this.right = new Node(this.x + i + 1, this.y, this.width - i - 1, this.height);
			} else {
				this.left = new Node(this.x, this.y, this.width, j);
				this.right = new Node(this.x, this.y + j + 1, this.width, this.height - j - 1);
			}
			return this.left.insert(glyphInfo);
		}
	}
}
