package dev.engine_room.flywheel.lib.model.baked;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.UnknownNullability;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;

class MeshEmitter {
	private final BufferBuilderStack bufferBuilderStack;

	private final List<BufferBuilder> activeBufferBuilders = new ArrayList<>();
	private final List<Material> activeKeys = new ArrayList<>();

	@UnknownNullability
	BlockMaterialFunction blockMaterialFunction;

	private int currentIndex = -1;

	MeshEmitter(BufferBuilderStack bufferBuilderStack) {
		this.bufferBuilderStack = bufferBuilderStack;
	}

	public void prepare(BlockMaterialFunction blockMaterialFunction) {
		this.blockMaterialFunction = blockMaterialFunction;
	}

	public void prepareForBlock() {
		// Quad render order within blocks must be preserved for correctness, however between blocks we should try to
		// reduce the number of generated meshes as much as possible. Here we reset the head index without flushing
		// any buffers, so that the next block can start over scanning through the parallel arrays looking for a
		// matching Material, BufferBuilder pair.
		currentIndex = -1;
	}

	public void end(ImmutableList.Builder<Model.ConfiguredMesh> out) {
		for (int index = 0; index < activeBufferBuilders.size(); index++) {
			var renderedBuffer = activeBufferBuilders.get(index)
					.endOrDiscardIfEmpty();

			if (renderedBuffer != null) {
				Material material = activeKeys.get(index);
				Mesh mesh = MeshHelper.blockVerticesToMesh(renderedBuffer, "source=ModelBuilder" + ",material=" + material);
				out.add(new Model.ConfiguredMesh(material, mesh));
				renderedBuffer.release();
			}
		}

		activeBufferBuilders.clear();
		activeKeys.clear();
		currentIndex = -1;
	}

	public BufferBuilder getBuffer(Material key) {
		if (currentIndex < 0 || !key.equals(activeKeys.get(currentIndex))) {
			while (true) {
				currentIndex++;

				if (currentIndex >= activeBufferBuilders.size()) {
					BufferBuilder bufferBuilder = bufferBuilderStack.getOrCreateBufferBuilder();
					bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
					activeBufferBuilders.add(bufferBuilder);
					activeKeys.add(key);
					break;
				}

				if (key.equals(activeKeys.get(currentIndex))) {
					break;
				}
			}
		}

		return activeBufferBuilders.get(currentIndex);
	}
}
