package com.jozufozu.flywheel.backend.material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.core.shader.IProgramCallback;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix4f;

public class MaterialGroup<P extends WorldProgram> {

	protected final MaterialManager<P> owner;
	protected final IRenderState state;

	private final ArrayList<MaterialRenderer<P>> renderers = new ArrayList<>();

	private final Map<MaterialSpec<?>, InstanceMaterial<?>> materials = new HashMap<>();

	public MaterialGroup(MaterialManager<P> owner, IRenderState state) {
		this.owner = owner;
		this.state = state;
	}

	public void render(Matrix4f viewProjection, double camX, double camY, double camZ, IProgramCallback<P> callback) {
		for (MaterialRenderer<P> renderer : renderers) {
			renderer.render(viewProjection, camX, camY, camZ, callback);
		}
	}

	@SuppressWarnings("unchecked")
	public <D extends InstanceData> InstanceMaterial<D> material(MaterialSpec<D> spec) {
		return (InstanceMaterial<D>) materials.computeIfAbsent(spec, this::createInstanceMaterial);
	}

	private InstanceMaterial<?> createInstanceMaterial(MaterialSpec<?> type) {
		InstanceMaterial<?> material = new InstanceMaterial<>(owner::getOriginCoordinate, type);

		this.renderers.add(new MaterialRenderer<>(owner.getProgram(type.getProgramName()), material));

		return material;
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
}
