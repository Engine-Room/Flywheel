package com.jozufozu.flywheel.core.vertex;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class InferredVertexFormatInfo {
	public final VertexFormat format;

	public final int positionOffset;
	public final int colorOffset;
	public final int textureOffset;
	public final int overlayOffset;
	public final int lightOffset;
	public final int normalOffset;

	public InferredVertexFormatInfo(VertexFormat format) {
		this.format = format;

		int positionOffset = -1;
		int colorOffset = -1;
		int textureOffset = -1;
		int overlayOffset = -1;
		int lightOffset = -1;
		int normalOffset = -1;

		int offset = 0;
		for (VertexFormatElement element : format.getElements()) {
			if (element == DefaultVertexFormat.ELEMENT_POSITION) {
				positionOffset = offset;
			} else if (element == DefaultVertexFormat.ELEMENT_COLOR) {
				colorOffset = offset;
			} else if (element == DefaultVertexFormat.ELEMENT_UV0) {
				textureOffset = offset;
			} else if (element == DefaultVertexFormat.ELEMENT_UV1) {
				overlayOffset = offset;
			} else if (element == DefaultVertexFormat.ELEMENT_UV2) {
				lightOffset = offset;
			} else if (element == DefaultVertexFormat.ELEMENT_NORMAL) {
				normalOffset = offset;
			}

			offset += element.getByteSize();
		}

		this.positionOffset = positionOffset;
		this.colorOffset = colorOffset;
		this.textureOffset = textureOffset;
		this.overlayOffset = overlayOffset;
		this.lightOffset = lightOffset;
		this.normalOffset = normalOffset;
	}
}
