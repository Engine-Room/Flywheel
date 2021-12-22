package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.model.ModelPool;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.Textures;
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

	private final Map<Instanced<? extends InstanceData>, InstancedMaterial<?>> materials = new HashMap<>();

	public InstancedMaterialGroup(InstancingEngine<P> owner, RenderType type) {
		this.owner = owner;
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <D extends InstanceData> InstancedMaterial<D> material(StructType<D> type) {
		if (type instanceof Instanced<D> instanced) {
			return (InstancedMaterial<D>) materials.computeIfAbsent(instanced, InstancedMaterial::new);
		} else {
			throw new ClassCastException("Cannot use type '" + type + "' with GPU instancing.");
		}
	}

	public void render(Matrix4f viewProjection, double camX, double camY, double camZ) {
		type.setupRenderState();
		Textures.bindActiveTextures();
		renderAll(viewProjection, camX, camY, camZ);
		type.clearRenderState();
	}

	protected void renderAll(Matrix4f viewProjection, double camX, double camY, double camZ) {
		for (Map.Entry<Instanced<? extends InstanceData>, InstancedMaterial<?>> entry : materials.entrySet()) {
			InstancedMaterial<?> material = entry.getValue();
			if (material.nothingToRender()) continue;

			Collection<? extends GPUInstancer<?>> instancers = material.models.asMap()
					.values();

			// initialize all uninitialized instancers...
			instancers.forEach(GPUInstancer::init);
			if (material.allocator instanceof ModelPool pool) {
				// ...and then flush the model arena in case anything was marked for upload
				pool.flush();
			}

			P program = owner.getProgram(entry.getKey()
					.getProgramSpec()).get();

			program.bind();
			program.uploadViewProjection(viewProjection);
			program.uploadCameraPos(camX, camY, camZ);

			setup(program);

			instancers.forEach(GPUInstancer::render);
		}
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
	}
}
