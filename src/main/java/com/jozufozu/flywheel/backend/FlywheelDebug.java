package com.jozufozu.flywheel.backend;

import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;

import com.mojang.blaze3d.platform.GlDebug;

public class FlywheelDebug {
//
//	public static void setup() {
//		GLDebugMessageCallback.create()
//		GL43.glDebugMessageCallback();
//	}
//
//	private static void printDebugLog(int p_84039_, int p_84040_, int p_84041_, int p_84042_, int p_84043_, long p_84044_, long p_84045_) {
//		String s = GLDebugMessageCallback.getMessage(p_84043_, p_84044_);
//		GlDebug.LogEntry gldebug$logentry;
//		synchronized(MESSAGE_BUFFER) {
//			gldebug$logentry = lastEntry;
//			if (gldebug$logentry != null && gldebug$logentry.isSame(p_84039_, p_84040_, p_84041_, p_84042_, s)) {
//				++gldebug$logentry.count;
//			} else {
//				gldebug$logentry = new GlDebug.LogEntry(p_84039_, p_84040_, p_84041_, p_84042_, s);
//				MESSAGE_BUFFER.add(gldebug$logentry);
//				lastEntry = gldebug$logentry;
//			}
//		}
//
//		LOGGER.info("OpenGL debug message: {}", (Object)gldebug$logentry);
//	}

}
