package com.jozufozu.flywheel.lib.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Axes {

	public static Axis XP = new Axis(new Vector3f(1, 0, 0));
	public static Axis YP = new Axis(new Vector3f(0, 1, 0));
	public static Axis ZP = new Axis(new Vector3f(0, 0, 1));
	public static Axis XN = new Axis(new Vector3f(-1, 0, 0));
	public static Axis YN = new Axis(new Vector3f(0, -1, 0));
	public static Axis ZN = new Axis(new Vector3f(0, 0, -1));

	public record Axis(Vector3fc vec) {
		public Quaternionf rotation(float radians) {
			return new Quaternionf().setAngleAxis(radians, vec.x(), vec.y(), vec.z());
		}

		public Quaternionf rotationDegrees(float degrees) {
			return rotation((float) Math.toRadians(degrees));
		}
	}
}
