package com.jozufozu.flywheel.util.extension;

import com.jozufozu.flywheel.util.joml.Matrix4f;

public interface Matrix4fExtension {

	Matrix4f flywheel$store(Matrix4f matrix);

	static Matrix4f clone(com.mojang.math.Matrix4f moj) {
		return ((Matrix4fExtension)(Object) moj).flywheel$store(new Matrix4f());
	}

	static void store(com.mojang.math.Matrix4f moj, Matrix4f joml) {
		((Matrix4fExtension)(Object) moj).flywheel$store(joml);
	}
}
