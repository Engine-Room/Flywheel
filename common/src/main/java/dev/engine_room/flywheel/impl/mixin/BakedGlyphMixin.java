package dev.engine_room.flywheel.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.engine_room.flywheel.lib.internal.GlyphExtension;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;

@Mixin(BakedGlyph.class)
public class BakedGlyphMixin implements GlyphExtension {
	@Shadow
	@Final
	private float u0;
	@Shadow
	@Final
	private float u1;
	@Shadow
	@Final
	private float v0;
	@Shadow
	@Final
	private float v1;
	@Shadow
	@Final
	private float left;
	@Shadow
	@Final
	private float right;
	@Shadow
	@Final
	private float up;
	@Shadow
	@Final
	private float down;

	@Unique
	private ResourceLocation flywheel$texture;

	@Override
	public float flywheel$u0() {
		return u0;
	}

	@Override
	public float flywheel$u1() {
		return u1;
	}

	@Override
	public float flywheel$v0() {
		return v0;
	}

	@Override
	public float flywheel$v1() {
		return v1;
	}

	@Override
	public float flywheel$left() {
		return left;
	}

	@Override
	public float flywheel$right() {
		return right;
	}

	@Override
	public float flywheel$up() {
		return up;
	}

	@Override
	public float flywheel$down() {
		return down;
	}

	@Override
	public ResourceLocation flywheel$texture() {
		return flywheel$texture;
	}

	@Override
	public void flywheel$texture(ResourceLocation location) {
		flywheel$texture = location;
	}
}
