package com.jozufozu.flywheel.core.model;

import static com.jozufozu.flywheel.util.RenderMath.nb;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.system.MemoryStack;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.core.Direction;
import com.mojang.math.Vector3f;
import net.minecraft.core.Vec3i;

public class BakedModelModel implements Model {
	// DOWN, UP, NORTH, SOUTH, WEST, EAST, null
	private static final Direction[] dirs;

	static {
		Direction[] directions = Direction.values();

		dirs = Arrays.copyOf(directions, directions.length + 1);
	}


	public final BakedModel model;
	private final int numQuads;

	public BakedModelModel(BakedModel model) {
		this.model = model;

		Random random = new Random();

		int numQuads = 0;

		for (Direction dir : dirs) {
			random.setSeed(42);
			List<BakedQuad> quads = model.getQuads(null, dir, random, VirtualEmptyModelData.INSTANCE);

			numQuads += quads.size();
		}

		this.numQuads = numQuads;
	}

	@Override
	public void buffer(VecBuffer buffer) {

		Minecraft mc = Minecraft.getInstance();

		ItemColors itemColors = mc.getItemColors();

		Random random = new Random();

		for (Direction dir : dirs) {
			random.setSeed(42);
			List<BakedQuad> quads = model.getQuads(null, dir, random, VirtualEmptyModelData.INSTANCE);

			for (BakedQuad bakedQuad : quads) {
//				int i = -1;
//				if (!itemStack.isEmpty() && bakedQuad.isTinted()) {
//					i = itemColors.getColor(itemStack, bakedQuad.getTintIndex());
//				}
//
//				byte red = (byte)(i >> 16 & 255);
//				byte green = (byte)(i >> 8 & 255);
//				byte blue = (byte)(i & 255);

				int[] aint = bakedQuad.getVertices();
				Vec3i faceNormal = bakedQuad.getDirection().getNormal();
				Vector3f normal = new Vector3f((float)faceNormal.getX(), (float)faceNormal.getY(), (float)faceNormal.getZ());
				int intSize = DefaultVertexFormat.BLOCK.getIntegerSize();
				int vertexCount = aint.length / intSize;

				try (MemoryStack memorystack = MemoryStack.stackPush()) {
					ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
					IntBuffer intbuffer = bytebuffer.asIntBuffer();

					for(int j = 0; j < vertexCount; ++j) {
						((Buffer)intbuffer).clear();
						intbuffer.put(aint, j * 8, 8);
						float f = bytebuffer.getFloat(0);
						float f1 = bytebuffer.getFloat(4);
						float f2 = bytebuffer.getFloat(8);
//						float cr;
//						float cg;
//						float cb;
//						float ca;
//						{
//							float r = (float)(bytebuffer.get(12) & 255) / 255.0F;
//							float g = (float)(bytebuffer.get(13) & 255) / 255.0F;
//							float b = (float)(bytebuffer.get(14) & 255) / 255.0F;
//							float a = (float)(bytebuffer.get(15) & 255) / 255.0F;
//							cr = r * red;
//							cg = g * green;
//							cb = b * blue;
//							ca = a;
//						}

						float u = bytebuffer.getFloat(16);
						float v = bytebuffer.getFloat(20);

						buffer.putVec3(f, f1, f2);
						buffer.putVec3(nb(normal.x()), nb(normal.y()), nb(normal.z()));
						buffer.putVec2(u, v);
					}
				}
			}
		}
	}

	@Override
	public int vertexCount() {
		return numQuads * 4;
	}

	@Override
	public VertexFormat format() {
		return Formats.UNLIT_MODEL;
	}
}
