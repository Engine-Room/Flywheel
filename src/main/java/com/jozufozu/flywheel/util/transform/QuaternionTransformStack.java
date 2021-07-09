package com.jozufozu.flywheel.util.transform;

import com.google.common.collect.Lists;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Stack;
import java.util.Vector;

public class QuaternionTransformStack implements TransformStack {

	private final Deque<Transform> stack;

	public QuaternionTransformStack() {
		stack = new ArrayDeque<>();
		stack.add(new Transform());
	}

	@Override
	public TransformStack translate(double x, double y, double z) {

		Transform peek = stack.peek();

		double qx = peek.qx;
		double qy = peek.qy;
		double qz = peek.qz;
		double qw = peek.qw;
		peek.x += qw * x + qy * z - qz * y;
		peek.y += qw * y - qx * z + qz * x;
		peek.z += qw * z + qx * y - qy * x;

		return this;
	}

	@Override
	public TransformStack multiply(Quaternion quaternion) {
		return this;
	}

	@Override
	public TransformStack push() {
		stack.push(stack.peek().copy());
		return this;
	}

	@Override
	public TransformStack pop() {

		if (stack.size() == 1) {
			stack.peek().loadIdentity();
		} else {
			stack.pop();
		}

		return this;
	}

	private static class Transform {
		public double qx;
		public double qy;
		public double qz;
		public double qw;
		public double x;
		public double y;
		public double z;

		public Transform() {
			qw = 1.0;
		}

		public void loadIdentity() {
			x = y = z = 0.0;

			qx = qy = qz = 0.0;
			qw = 1.0;
		}

		public Transform copy() {
			Transform transform = new Transform();

			transform.qx = this.qx;
			transform.qy = this.qy;
			transform.qz = this.qz;
			transform.qw = this.qw;
			transform.x = this.x;
			transform.y = this.y;
			transform.z = this.z;

			return transform;
		}
	}
}
