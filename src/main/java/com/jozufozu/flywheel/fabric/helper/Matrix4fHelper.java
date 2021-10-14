package com.jozufozu.flywheel.fabric.helper;

import com.jozufozu.flywheel.fabric.extension.Matrix4fExtension;
import com.mojang.math.Matrix4f;

public final class Matrix4fHelper {
	public static void multiplyBackward(Matrix4f self, Matrix4f other) {
		Matrix4f copy = other.copy();
		copy.multiply(self);
		self.load(copy);
	}

	public static void setTranslation(Matrix4f self, float x, float y, float z) {
		((Matrix4fExtension) (Object) self).setTranslation(x, y, z);
	}
}
