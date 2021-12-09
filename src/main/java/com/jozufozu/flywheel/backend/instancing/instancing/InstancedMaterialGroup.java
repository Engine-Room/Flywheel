package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.api.InstanceData;
import com.jozufozu.flywheel.backend.api.MaterialGroup;
import com.jozufozu.flywheel.backend.api.MaterialSpec;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.TextureBinder;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.RenderType;

/**
 * A group of materials all rendered with the same GL state.
 *
 * The children of a material group will all be rendered at the same time.
 * No guarantees are made about the order of draw calls.
 */
public class InstancedMaterialGroup<P extends WorldProgram> implements MaterialGroup {

	protected final InstancingEngine<P> owner;
	protected final RenderType type;

	protected final ArrayList<InstancedMaterialRenderer<P>> renderers = new ArrayList<>();

	private final Map<MaterialSpec<?>, InstancedMaterial<?>> materials = new HashMap<>();

	public InstancedMaterialGroup(InstancingEngine<P> owner, RenderType type) {
		this.owner = owner;
		this.type = type;
	}

	/**
	 * Get the material as defined by the given {@link MaterialSpec spec}.
	 * @param spec The material you want to create instances with.
	 * @param <D> The type representing the per instance data.
	 * @return A
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <D extends InstanceData> InstancedMaterial<D> material(MaterialSpec<D> spec) {
		return (InstancedMaterial<D>) materials.computeIfAbsent(spec, this::createInstanceMaterial);
	}

	public void render(Matrix4f viewProjection, double camX, double camY, double camZ) {
		type.setupRenderState();
		TextureBinder.bindActiveTextures();
		for (InstancedMaterialRenderer<P> renderer : renderers) {
			renderer.render(viewProjection, camX, camY, camZ);
		}
		type.clearRenderState();
	}

	public void setup(P program) {

	}

	public void clear() {
		materials.values().forEach(InstancedMaterial::clear);
	}

	public void delete() {
		materials.values()
				.forEach(InstancedMaterial::delete);

		materials.clear();
		renderers.clear();
	}

	private InstancedMaterial<?> createInstanceMaterial(MaterialSpec<?> type) {
		InstancedMaterial<?> material = new InstancedMaterial<>(type);

		this.renderers.add(new InstancedMaterialRenderer<>(owner.getProgram(type.getProgramName()), material, this::setup));

		return material;
	}
}
