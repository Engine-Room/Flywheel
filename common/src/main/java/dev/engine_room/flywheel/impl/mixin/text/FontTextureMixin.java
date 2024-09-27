package dev.engine_room.flywheel.impl.mixin.text;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.impl.FontTextureUpload;
import dev.engine_room.flywheel.lib.internal.FontTextureExtension;
import dev.engine_room.flywheel.lib.internal.GlyphExtension;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

@Mixin(FontTexture.class)
public abstract class FontTextureMixin extends AbstractTexture implements FontTextureExtension {
	@Unique
	private final List<FontTextureUpload> flywheel$uploads = new ArrayList<>();
	@Unique
	private boolean flywheel$flushScheduled = false;

	@Unique
	private ResourceLocation flywheel$name;

	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontTexture;getId()I"))
	private int flywheel$skipGetId(FontTexture instance, Operation<Integer> original) {
		// getId lazily creates the texture id, which is good,
		// but it doesn't check for the render thread, which explodes.
		if (RenderSystem.isOnRenderThreadOrInit()) {
			return original.call(instance);
		}
		// We'll call getId manually in the recorded render call below.
		return 0;
	}

	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;prepareImage(Lcom/mojang/blaze3d/platform/NativeImage$InternalGlFormat;III)V"))
	private void flywheel$skipPrepareImage(NativeImage.InternalGlFormat arg, int i, int j, int k, Operation<Void> original) {
		if (RenderSystem.isOnRenderThreadOrInit()) {
			original.call(arg, i, j, k);
		} else {
			RenderSystem.recordRenderCall(() -> original.call(arg, getId(), j, k));
		}
	}

	@WrapWithCondition(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontTexture;bind()V"))
	private boolean flywheel$onlyOnRenderThreadOrInitBindAndUpload(FontTexture instance) {
		return RenderSystem.isOnRenderThreadOrInit();
	}

	@WrapWithCondition(method = "add", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/font/SheetGlyphInfo;upload(II)V"))
	private boolean flywheel$onlyOnRenderThreadOrInitBindAndUpload2(SheetGlyphInfo instance, int x, int y) {
		return RenderSystem.isOnRenderThreadOrInit();
	}

	@WrapOperation(method = "add", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/font/FontTexture$Node;x:I", ordinal = 0))
	private int flywheel$shareNode(@Coerce Object instance, Operation<Integer> original, @Share("node") LocalRef<Object> node) {
		node.set(instance);
		return original.call(instance);
	}

	@Inject(method = "add", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/font/SheetGlyphInfo;upload(II)V", shift = At.Shift.AFTER))
	private void flywheel$uploadOrFlush(SheetGlyphInfo glyphInfo, CallbackInfoReturnable<BakedGlyph> cir, @Share("node") LocalRef<Object> node) {
		FontTexture$NodeAccessor accessor = ((FontTexture$NodeAccessor) node.get());

		// Shove all the uploads into a list to be processed as a batch.
		// Saves a lot of lambda allocations that would be spent binding the same texture over and over.
		flywheel$uploads.add(new FontTextureUpload(glyphInfo, accessor.flywheel$getX(), accessor.flywheel$getY()));

		if (!flywheel$flushScheduled) {
			RenderSystem.recordRenderCall(this::flywheel$flush);
			flywheel$flushScheduled = true;
		}
	}

	@ModifyExpressionValue(method = "add", at = @At(value = "NEW", target = "net/minecraft/client/gui/font/glyphs/BakedGlyph"))
	private BakedGlyph flywheel$setGlyphExtensionName(BakedGlyph original) {
		((GlyphExtension) original).flywheel$texture(flywheel$name);
		return original;
	}

	@Unique
	public void flywheel$flush() {
		this.bind();
		for (FontTextureUpload upload : flywheel$uploads) {
			upload.info()
					.upload(upload.x(), upload.y());
		}

		flywheel$uploads.clear();

		flywheel$flushScheduled = false;
	}

	@Override
	public void flywheel$setName(ResourceLocation value) {
		flywheel$name = value;
	}
}
