package com.jozufozu.flywheel.impl;

import com.jozufozu.flywheel.backend.engine.uniform.DebugMode;

import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class DebugModeArgument extends StringRepresentableArgument<DebugMode> {
	public static final DebugModeArgument INSTANCE = new DebugModeArgument();
	public static final SingletonArgumentInfo<DebugModeArgument> INFO = SingletonArgumentInfo.contextFree(() -> INSTANCE);

	public DebugModeArgument() {
		super(DebugMode.CODEC, DebugMode::values);
	}
}
