package com.jozufozu.flywheel.backend.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public class TextureRenderState implements IRenderState {
	private static final Map<Pair<GlTextureUnit, ResourceLocation>, TextureRenderState> states = new HashMap<>();

	public final GlTextureUnit unit;
	public final ResourceLocation location;

	private TextureRenderState(GlTextureUnit unit, ResourceLocation location) {
		this.unit = unit;
		this.location = location;
	}

	public static TextureRenderState get(ResourceLocation texture) {
		return get(GlTextureUnit.T0, texture);
	}

	public static TextureRenderState get(GlTextureUnit unit, ResourceLocation texture) {
		return states.computeIfAbsent(Pair.of(unit, texture), p -> new TextureRenderState(p.getFirst(), p.getSecond()));
	}

	@Override
	public void bind() {
		unit.makeActive();
		Minecraft.getInstance().getTextureManager().bindForSetup(location);
	}

	@Override
	public void unbind() {

	}

	@Nullable
	@Override
	public ResourceLocation getTexture(GlTextureUnit textureUnit) {
		if (textureUnit == unit) return location;
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TextureRenderState that = (TextureRenderState) o;
		return location.equals(that.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(location);
	}
}
