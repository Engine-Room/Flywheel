package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.lib.material.MaterialIndices;
import com.jozufozu.flywheel.util.Textures;

public class IndirectDrawSet<T extends InstancedPart> {

	final List<IndirectDraw<T>> indirectDraws = new ArrayList<>();

	final Map<RenderStage, List<MultiDraw>> multiDraws = new EnumMap<>(RenderStage.class);

	public boolean isEmpty() {
		return indirectDraws.isEmpty();
	}

	public int size() {
		return indirectDraws.size();
	}

	public void add(IndirectInstancer<T> instancer, Material material, RenderStage stage, IndirectMeshPool.BufferedMesh bufferedMesh) {
		indirectDraws.add(new IndirectDraw<>(instancer, material, stage, bufferedMesh));
		determineMultiDraws();
	}

	public void submit(RenderStage stage) {
		if (!multiDraws.containsKey(stage)) {
			return;
		}

		for (var multiDraw : multiDraws.get(stage)) {
			multiDraw.submit();
		}
	}

	public void determineMultiDraws() {
		// TODO: Better material equality. Really we only need to bin by the results of the setup method.
		multiDraws.clear();
		// sort by stage, then material
		indirectDraws.sort(Comparator.comparing(IndirectDraw<T>::stage)
				.thenComparing(draw -> MaterialIndices.getMaterialIndex(draw.material())));

		for (int start = 0, i = 0; i < indirectDraws.size(); i++) {
			var draw = indirectDraws.get(i);
			var material = draw.material();
			var stage = draw.stage();

			// if the next draw call has a different RenderStage or Material, start a new MultiDraw
			if (i == indirectDraws.size() - 1 || stage != indirectDraws.get(i + 1)
					.stage() || !material.equals(indirectDraws.get(i + 1)
					.material())) {
				multiDraws.computeIfAbsent(stage, s -> new ArrayList<>())
						.add(new MultiDraw(material, start, i + 1));
				start = i + 1;
			}
		}
	}

	public boolean contains(RenderStage stage) {
		return multiDraws.containsKey(stage);
	}

	private record MultiDraw(Material material, int start, int end) {
		void submit() {
			material.setup();
			Textures.bindActiveTextures();
			glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, start * IndirectBuffers.DRAW_COMMAND_STRIDE, end - start, (int) IndirectBuffers.DRAW_COMMAND_STRIDE);
			material.clear();
		}
	}
}
