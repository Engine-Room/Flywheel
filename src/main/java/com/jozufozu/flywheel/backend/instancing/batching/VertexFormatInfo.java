package com.jozufozu.flywheel.backend.instancing.batching;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class VertexFormatInfo {
	public final VertexFormat format;
	public final int stride;

	public final int positionOffset;
	public final int colorOffset;
	public final int textureOffset;
	public final int overlayOffset;
	public final int lightOffset;
	public final int normalOffset;

	public VertexFormatInfo(VertexFormat format) {
		this.format = format;
		stride = format.getVertexSize();

		int positionOffset = -1;
		int colorOffset = -1;
		int textureOffset = -1;
		int overlayOffset = -1;
		int lightOffset = -1;
		int normalOffset = -1;

		int offset = 0;
		for (VertexFormatElement element : format.getElements()) {
			switch (element.getUsage()) {
			case POSITION -> positionOffset = offset;
			case NORMAL -> normalOffset = offset;
			case COLOR -> colorOffset = offset;
			case UV -> {
				switch (element.getIndex()) {
					case 0 -> textureOffset = offset;
					case 1 -> overlayOffset = offset;
					case 2 -> lightOffset = offset;
				}
			}
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

	protected VertexFormatInfo(VertexFormatInfo formatInfo) {
		format = formatInfo.format;
		stride = formatInfo.stride;
		positionOffset = formatInfo.positionOffset;
		colorOffset = formatInfo.colorOffset;
		textureOffset = formatInfo.textureOffset;
		overlayOffset = formatInfo.overlayOffset;
		lightOffset = formatInfo.lightOffset;
		normalOffset = formatInfo.normalOffset;
	}
}
