package com.jozufozu.flywheel.backend.material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.util.math.vector.Matrix4f;

/**
 * A group of materials all rendered with the same GL state.
 *
 * The children of a material group will all be rendered at the same time.
 * No guarantees are made about the order of draw calls.
 */
public class MaterialGroupImpl<P extends WorldProgram> implements MaterialGroup {

	protected final MaterialManagerImpl<P> owner;
	protected final IRenderState state;

	private final ArrayList<MaterialRenderer<P>> renderers = new ArrayList<>();

	private final Map<MaterialSpec<?>, InstanceMaterialImpl<?>> materials = new HashMap<>();

	public MaterialGroupImpl(MaterialManagerImpl<P> owner, IRenderState state) {
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
	@Override
	public <D extends InstanceData> InstanceMaterialImpl<D> material(MaterialSpec<D> spec) {
		return (InstanceMaterialImpl<D>) materials.computeIfAbsent(spec, this::createInstanceMaterial);
	}

	public void render(Matrix4f viewProjection, double camX, double camY, double camZ) {
		for (MaterialRenderer<P> renderer : renderers) {
			renderer.render(viewProjection, camX, camY, camZ);
		}
	}

	public void setup(P program) {

	}

	public void clear() {
		materials.values().forEach(InstanceMaterialImpl::clear);
	}

	public void delete() {
		materials.values()
				.forEach(InstanceMaterialImpl::delete);

		materials.clear();
		renderers.clear();
	}

	private InstanceMaterialImpl<?> createInstanceMaterial(MaterialSpec<?> type) {
		InstanceMaterialImpl<?> material = new InstanceMaterialImpl<>(type);

		this.renderers.add(new MaterialRenderer<>(owner.getProgram(type.getProgramName()), material, this::setup));

		return material;
	}
}
