package com.jozufozu.flywheel.backend.instancing.indirect;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;

public class IndirectDrawSet<T extends InstancedPart> {

	final List<IndirectDraw<T>> indirectDraws = new ArrayList<>();

	public boolean isEmpty() {
		return indirectDraws.isEmpty();
	}

	public int size() {
		return indirectDraws.size();
	}

	public void add(IndirectInstancer<T> instancer, Material material, RenderStage stage, IndirectMeshPool.BufferedMesh bufferedMesh) {
		indirectDraws.add(new IndirectDraw<>(instancer, material, stage, bufferedMesh));
	}

	public void submit(RenderStage stage) {
		final int stride = (int) IndirectBuffers.DRAW_COMMAND_STRIDE;
		for (int i = 0, indirectDrawsSize = indirectDraws.size(); i < indirectDrawsSize; i++) {
			var batch = indirectDraws.get(i);
			if (batch.stage != stage) {
				continue;
			}

			var material = batch.material;
			material.setup();
			glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, i * stride, 1, stride);
			material.clear();
		}
	}

	public boolean contains(RenderStage stage) {
		for (var draw : indirectDraws) {
			if (draw.stage == stage) {
				return true;
			}
		}

		return false;
	}
}
