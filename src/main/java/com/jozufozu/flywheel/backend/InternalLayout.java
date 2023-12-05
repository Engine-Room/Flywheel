package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.lib.vertex.FullVertex;
import com.jozufozu.flywheel.lib.vertex.FullVertexList;

public class InternalLayout {
	public static final BufferLayout LAYOUT = FullVertex.FORMAT;

	public static ReusableVertexList createVertexList() {
		return new FullVertexList();
	}
}
