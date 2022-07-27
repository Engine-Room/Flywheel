package com.jozufozu.flywheel.util.extension;

import com.jozufozu.flywheel.util.joml.Matrix3f;

public interface Matrix3fExtension {

	Matrix3f flywheel$store(Matrix3f matrix);

	static Matrix3f clone(com.mojang.math.Matrix3f moj) {
		return ((Matrix3fExtension)(Object) moj).flywheel$store(new Matrix3f());
	}

	static void store(com.mojang.math.Matrix3f moj, Matrix3f joml) {
		((Matrix3fExtension)(Object) moj).flywheel$store(joml);
	}
}
