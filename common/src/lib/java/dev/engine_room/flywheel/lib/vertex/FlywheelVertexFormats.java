package dev.engine_room.flywheel.lib.vertex;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class FlywheelVertexFormats {
	/// Basically the same as {@link DefaultVertexFormat#BLOCK} but with the normals included
	public static final VertexFormat BLOCK_VERTEX_FORMAT = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color",VertexFormatElement.COLOR)
			.add("UV0",VertexFormatElement.UV0)
			.add("UV2",VertexFormatElement.UV2)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.build();
}
