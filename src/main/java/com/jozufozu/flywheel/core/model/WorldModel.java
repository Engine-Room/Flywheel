package com.jozufozu.flywheel.core.model;

import java.util.Collection;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class WorldModel implements IModel {

	private final BufferBuilderReader reader;
	private final String name;

	public WorldModel(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks, String name) {
		reader = new BufferBuilderReader(ModelUtil.getBufferBuilderFromTemplate(renderWorld, layer, blocks));
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void buffer(VertexConsumer vertices) {
		for (int i = 0; i < vertexCount(); i++) {
			vertices.vertex(reader.getX(i), reader.getY(i), reader.getZ(i));

			vertices.normal(RenderMath.f(reader.getNX(i)), RenderMath.f(reader.getNY(i)), RenderMath.f(reader.getNZ(i)));

			vertices.uv(reader.getU(i), reader.getV(i));

			vertices.color(reader.getR(i), reader.getG(i), reader.getB(i), reader.getA(i));

			int light = reader.getLight(i);

			byte block = (byte) (LightTexture.block(light) << 4);
			byte sky = (byte) (LightTexture.sky(light) << 4);

			vertices.uv2(block, sky);

			vertices.endVertex();
		}
	}

	@Override
	public int vertexCount() {
		return reader.getVertexCount();
	}

	@Override
	public VertexFormat format() {
		return Formats.COLORED_LIT_MODEL;
	}

}
