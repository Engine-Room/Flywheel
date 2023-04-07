package com.jozufozu.flywheel.lib.vertex;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.lib.math.MatrixUtil;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

public final class VertexTransformations {
	public static void transformPos(MutableVertexList vertexList, int index, Matrix4f matrix) {
		float x = vertexList.x(index);
		float y = vertexList.y(index);
		float z = vertexList.z(index);
		vertexList.x(index, MatrixUtil.transformPositionX(matrix, x, y, z));
		vertexList.y(index, MatrixUtil.transformPositionY(matrix, x, y, z));
		vertexList.z(index, MatrixUtil.transformPositionZ(matrix, x, y, z));
	}

	public static void transformNormal(MutableVertexList vertexList, int index, Matrix3f matrix) {
		float nx = vertexList.normalX(index);
		float ny = vertexList.normalY(index);
		float nz = vertexList.normalZ(index);
		float tnx = MatrixUtil.transformNormalX(matrix, nx, ny, nz);
		float tny = MatrixUtil.transformNormalY(matrix, nx, ny, nz);
		float tnz = MatrixUtil.transformNormalZ(matrix, nx, ny, nz);
		float sqrLength = tnx * tnx + tny * tny + tnz * tnz;
		if (sqrLength != 0) {
			float f = 1 / (float) Math.sqrt(sqrLength);
			tnx *= f;
			tny *= f;
			tnz *= f;
		}
		vertexList.normalX(index, tnx);
		vertexList.normalY(index, tny);
		vertexList.normalZ(index, tnz);
	}
}
