package dev.engine_room.flywheel.backend.engine.instancing;

import static dev.engine_room.flywheel.backend.engine.instancing.InstancedDrawManager.uploadMaterialUniform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.InstancingPrograms;
import dev.engine_room.flywheel.backend.engine.GroupKey;
import dev.engine_room.flywheel.backend.engine.MaterialRenderState;
import dev.engine_room.flywheel.backend.gl.TextureBuffer;

public class InstancedRenderStage {
	private static final Comparator<InstancedDraw> DRAW_COMPARATOR = Comparator.comparing(InstancedDraw::bias)
			.thenComparing(InstancedDraw::indexOfMeshInModel)
			.thenComparing(InstancedDraw::material, MaterialRenderState.COMPARATOR);

	private final Map<GroupKey<?>, DrawGroup> groups = new HashMap<>();

	public InstancedRenderStage() {
	}

	public void delete() {
		groups.values()
				.forEach(DrawGroup::delete);
		groups.clear();
	}

	public void put(GroupKey<?> groupKey, InstancedDraw instancedDraw) {
		groups.computeIfAbsent(groupKey, $ -> new DrawGroup())
				.put(instancedDraw);
	}

	public boolean isEmpty() {
		return groups.isEmpty();
	}

	public void flush() {
		groups.values()
				.forEach(DrawGroup::flush);

		groups.values()
				.removeIf(DrawGroup::isEmpty);
	}

	public void draw(TextureBuffer instanceTexture, InstancingPrograms programs) {
		for (var entry : groups.entrySet()) {
			var shader = entry.getKey();
			var drawCalls = entry.getValue();

			var environment = shader.environment();

			for (var drawCall : drawCalls.draws) {
				var material = drawCall.material();

				var program = programs.get(shader.instanceType(), environment.contextShader(), material);
				program.bind();

				environment.setupDraw(program);

				uploadMaterialUniform(program, material);

				MaterialRenderState.setup(material);

				Samplers.INSTANCE_BUFFER.makeActive();

				drawCall.render(instanceTexture);
			}
		}
	}

	public static class DrawGroup {
		private final List<InstancedDraw> draws = new ArrayList<>();
		private boolean needSort = false;

		public void put(InstancedDraw instancedDraw) {
			draws.add(instancedDraw);
			needSort = true;
		}

		public void delete() {
			draws.forEach(InstancedDraw::delete);
			draws.clear();
		}

		public void flush() {
			needSort |= draws.removeIf(InstancedDraw::deleted);

			if (needSort) {
				draws.sort(DRAW_COMPARATOR);
				needSort = false;
			}
		}

		public boolean isEmpty() {
			return draws.isEmpty();
		}
	}
}
