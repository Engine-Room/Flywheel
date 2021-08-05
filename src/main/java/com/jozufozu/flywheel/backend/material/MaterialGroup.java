package com.jozufozu.flywheel.backend.material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import com.mojang.math.Matrix4f;

/**
 * A group of materials all rendered with the same GL state.
 *
 * The children of a material group will all be rendered at the same time.
 * No guarantees are made about the order of draw calls.
 */
public class MaterialGroup<P extends WorldProgram> {

	protected final MaterialManager<P> owner;
	protected final IRenderState state;

	private final ArrayList<MaterialRenderer<P>> renderers = new ArrayList<>();

	private final Map<MaterialSpec<?>, InstanceMaterial<?>> materials = new HashMap<>();

	public MaterialGroup(MaterialManager<P> owner, IRenderState state) {
		this.owner = owner;
		this.state = state;
	}

	/**
	 * Get the material as defined by the given {@link MaterialSpec spec}.
	 * @param spec The material you want to create instances with.
	 * @param <D> The type representing the per instance data.
	 * @return A
	 */
	@SuppressWarnings("unchecked")
	public <D extends InstanceData> InstanceMaterial<D> material(MaterialSpec<D> spec) {
		return (InstanceMaterial<D>) materials.computeIfAbsent(spec, this::createInstanceMaterial);
	}

	public void render(Matrix4f viewProjection, double camX, double camY, double camZ) {
		for (MaterialRenderer<P> renderer : renderers) {
			renderer.render(viewProjection, camX, camY, camZ);
		}
	}

	public void setup(P program) {

	}

	public void clear() {
		materials.values().forEach(InstanceMaterial::clear);
	}

	public void delete() {
		materials.values()
				.forEach(InstanceMaterial::delete);

		materials.clear();
		renderers.clear();
	}

	private InstanceMaterial<?> createInstanceMaterial(MaterialSpec<?> type) {
		InstanceMaterial<?> material = new InstanceMaterial<>(type);

		this.renderers.add(new MaterialRenderer<>(owner.getProgram(type.getProgramName()), material, this::setup));

		return material;
	}
}
