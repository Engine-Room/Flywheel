package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.model.FallbackAllocator;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.backend.model.ModelPool;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.compile.ProgramContext;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.Textures;

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

	private ModelAllocator allocator;
	private int vertexCount;
	private int instanceCount;

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

	/**
	 * Get the number of instances drawn last frame.
	 * @return The instance count.
	 */
	public int getInstanceCount() {
		return instanceCount;
	}

	/**
	 * Get the number of vertices drawn last frame.
	 * @return The vertex count.
	 */
	public int getVertexCount() {
		return vertexCount;
	}

	// XXX Overriden in CrumblingGroup
	// XXX Runs inside of restore state
	public void render(Matrix4f viewProjection, double camX, double camY, double camZ, RenderLayer layer) {
		type.setupRenderState();
		Textures.bindActiveTextures(); // XXX Changes active unit and bound textures
		renderAll(viewProjection, camX, camY, camZ, layer); // XXX May change ARRAY_BUFFER binding (reset or not reset), VAO binding (not reset), shader binding (not reset), call Model.createEBO
		type.clearRenderState();
		// XXX Should texture bindings be reset or restored?
		// XXX Should the active unit be reset or restored?
		// XXX Should the VAO binding be reset or restored?
		// XXX Should the ARRAY_BUFFER binding be reset or restored?
		// XXX Should the shader binding be reset or restored?
	}

	// XXX Internal GL state changes are inconsistent; sometimes bindings are reset to 0, sometimes not
	protected void renderAll(Matrix4f viewProjection, double camX, double camY, double camZ, RenderLayer layer) {
		initializeInstancers(); // XXX May change ARRAY_BUFFER binding (reset or not reset), VAO binding (not reset), call Model.createEBO

		vertexCount = 0;
		instanceCount = 0;

		for (Map.Entry<Instanced<? extends InstanceData>, InstancedMaterial<?>> entry : materials.entrySet()) {
			InstancedMaterial<?> material = entry.getValue();
			if (material.nothingToRender()) continue;

			P program = owner.context.getProgram(ProgramContext.create(entry.getKey()
					.getProgramSpec(), Formats.BLOCK, layer));

			// XXX Shader is bound and not reset or restored
			program.bind();
			program.uploadViewProjection(viewProjection);
			program.uploadCameraPos(camX, camY, camZ);

			setup(program);

			for (GPUInstancer<?> instancer : material.getAllInstancers()) {
				instancer.render(); // XXX May change VAO binding (not reset), ARRAY_BUFFER binding (reset)
				vertexCount += instancer.getVertexCount();
				instanceCount += instancer.getInstanceCount();
			}
		}
	}

	private void initializeInstancers() {
		ModelAllocator allocator = getModelAllocator(); // XXX May change ARRAY_BUFFER binding (not reset)

		// initialize all uninitialized instancers...
		for (InstancedMaterial<?> material : materials.values()) {
			for (GPUInstancer<?> instancer : material.uninitialized) {
				instancer.init(allocator); // XXX May change VAO binding (not reset), ARRAY_BUFFER binding (not reset), call Model.createEBO
			}
			material.uninitialized.clear();
		}

		if (allocator instanceof ModelPool pool) {
			// ...and then flush the model arena in case anything was marked for upload
			pool.flush(); // XXX May change ARRAY_BUFFER binding (reset)
		}
	}

	protected void setup(P program) {

	}

	public void clear() {
		materials.values().forEach(InstancedMaterial::clear);
	}

	public void delete() {
		materials.values()
				.forEach(InstancedMaterial::delete);

		materials.clear();
	}

	private ModelAllocator getModelAllocator() {
		if (allocator == null) {
			allocator = createAllocator();
		}
		return this.allocator;
	}

	private static ModelAllocator createAllocator() {
		if (GlCompat.getInstance()
				.onAMDWindows()) {
			return FallbackAllocator.INSTANCE;
		} else {
			return new ModelPool(Formats.BLOCK);
		}
	}
}
