package com.jozufozu.flywheel.core.model;

import java.util.Collection;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Formats;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class WorldModel implements Model {

	private final VertexList reader;
	private final String name;

	/**
	 * It is expected that <code>renderWorld.getShade(...)</code> returns a constant.
	 */
	public WorldModel(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks, String name) {
		reader = Formats.BLOCK.createReader(ModelUtil.getBufferBuilderFromTemplate(renderWorld, layer, blocks));
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public VertexType getType() {
		return Formats.BLOCK;
	}

	@Override
	public int vertexCount() {
		return reader.getVertexCount();
	}

	@Override
	public VertexList getReader() {
		return reader;
	}
}
