package com.jozufozu.flywheel.backend;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

public class FlywheelMemory {

	public static final Cleaner CLEANER = Cleaner.create();

	private static int gpuMemory = 0;
	private static int cpuMemory = 0;

	public static void _freeCPUMemory(long size) {
		cpuMemory -= size;
	}

	public static void _allocCPUMemory(long size) {
		cpuMemory += size;
	}

	public static void _freeGPUMemory(long size) {
		gpuMemory -= size;
	}

	public static void _allocGPUMemory(long size) {
		gpuMemory += size;
	}

	public static int getGPUMemory() {
		return gpuMemory;
	}

	public static int getCPUMemory() {
		return cpuMemory;
	}

	public static Cleaner.Cleanable track(Object owner, ByteBuffer buffer) {
		return CLEANER.register(owner, new Tracked(buffer));
	}

	public static class Tracked implements Runnable {

		private final ByteBuffer buffer;

		public Tracked(ByteBuffer buffer) {
			this.buffer = buffer;
			_allocCPUMemory(buffer.capacity());
		}

		public void run() {
			_freeCPUMemory(buffer.capacity());
			MemoryUtil.memFree(buffer);
		}
	}
}
