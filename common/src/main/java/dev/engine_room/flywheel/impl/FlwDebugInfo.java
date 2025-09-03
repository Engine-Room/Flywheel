package dev.engine_room.flywheel.impl;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.impl.visualization.VisualizationManagerImpl;
import dev.engine_room.flywheel.lib.memory.FlwMemoryTracker;
import dev.engine_room.flywheel.lib.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public final class FlwDebugInfo {

	private FlwDebugInfo() {
	}

	/**
	 * Append a debug data point to the given StringBuilder, formatted as a Markdown list item.
	 */
	public static StringBuilder appendLine(StringBuilder dst, String str) {
		dst.append("\n- ");
		dst.append(str);
		return dst;
	}

	/**
	 * Append a header to the given StringBuilder, preceded by two new lines for separation.
	 */
	public static void appendHeader(StringBuilder dst, String str) {
		dst.append("\n\n## ");
		dst.append(str);
	}

	public static Component getDebugCommandInfo() {
		StringBuilder out = new StringBuilder("# `/flywheel debug info`");

		VisualizationManagerImpl manager = VisualizationManagerImpl.get(Minecraft.getInstance().level);

		addImplDebugInfo(out);
		addSystemDebugInfo(out);
		addOpenGLDebugInfo(out);
		addBackendDebugInfo(manager, out);
		addVisualizationManagerDebugInfo(manager, out);

		// Write out to a string both to emit to chat and include in the click event.
		String debugInfoString = out.toString();

		return Component.literal(debugInfoString)
				.append(Component.literal("\n\nClick to copy debug info to clipboard")
						.withStyle(Style.EMPTY.withUnderlined(true)
								.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, debugInfoString))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(debugInfoString)))))
				.append(Component.literal("\n\nClick to open an issue on GitHub")
						.withStyle(Style.EMPTY.withUnderlined(true)
								.withColor(ChatFormatting.BLUE)
								.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Engine-Room/Flywheel/issues/new"))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Opens URL:\nhttps://github.com/Engine-Room/Flywheel/issues/new")))));

	}

	private static void addBackendDebugInfo(@Nullable VisualizationManagerImpl manager, StringBuilder out) {
		appendHeader(out, "Backend");

		if (manager == null) {
			appendLine(out, "No visualization manager found");
			return;
		}

		var engineImpl = manager.getEngineImpl();

		if (engineImpl == null) {
			appendLine(out, "Third party backend in use");
			return;
		}

		appendLine(out, "Environments: ").append(engineImpl.environmentStorage().arena.occupancy())
				.append(" / ")
				.append(engineImpl.environmentStorage().arena.capacity());

		appendLine(out, "Light Sections: ").append(engineImpl.lightStorage().arena.occupancy())
				.append(" / ")
				.append(engineImpl.lightStorage().arena.capacity());

		var lut = engineImpl.lightStorage()
				.createLut();

		appendLine(out, "Light LUT Size: ").append(lut.size() * Integer.BYTES);
	}

	private static void addVisualizationManagerDebugInfo(@Nullable VisualizationManagerImpl manager, StringBuilder out) {
		if (manager == null) {
			out.append('\n');
			appendHeader(out, "Visualization Manager");
			appendLine(out, "No visualization manager found");
			return;
		}

		appendHeader(out, "Client Level Visualization Manager");
		Vec3i renderOrigin = manager.renderOrigin();
		appendLine(out, "Origin: ").append(renderOrigin.getX())
				.append(", ")
				.append(renderOrigin.getY())
				.append(", ")
				.append(renderOrigin.getZ());
		appendLine(out, "Block Entity Visuals: ").append(manager.blockEntities()
				.visualCount());
		appendLine(out, "Entity Visuals: ").append(manager.entities()
				.visualCount());
		appendLine(out, "Effect Visuals: ").append(manager.effects()
				.visualCount());
	}

	private static void addImplDebugInfo(StringBuilder out) {
		appendHeader(out, "Impl");

		appendLine(out, "Flywheel Version: ").append(FlwImplXplat.INSTANCE.getVersionStr());
		appendLine(out, "Backend: ").append(BackendManagerImpl.getBackendString());
		appendLine(out, "Update limiting: ").append(FlwConfig.INSTANCE.limitUpdates() ? "on" : "off");
	}

	private static void addSystemDebugInfo(StringBuilder out) {
		appendHeader(out, "System Info");
		appendLine(out, "Java Version: ").append(System.getProperty("java.version"));
		appendLine(out, "Java VM: ").append(System.getProperty("java.vm.name"))
				.append(" (")
				.append(System.getProperty("java.vm.version"))
				.append(")");
		appendLine(out, "OS: ").append(System.getProperty("os.name"))
				.append(" (")
				.append(System.getProperty("os.arch"))
				.append(")");
		appendLine(out, "Flw CPU Memory: ").append(FlwMemoryTracker.getCpuMemory());
		appendLine(out, "Flw GPU Memory: ").append(FlwMemoryTracker.getGpuMemory());
	}

	private static void addOpenGLDebugInfo(StringBuilder out) {
		appendHeader(out, "OpenGL");
		appendLine(out, "Vendor: ").append(GlCompat.GL_VENDOR_STRING);
		appendLine(out, "Renderer: ").append(GlCompat.GL_RENDERER_STRING);
		appendLine(out, "Version: ").append(GlCompat.GL_VERSION_STRING);
		appendLine(out, "Shading Language Version: ").append(GlCompat.GL_SHADING_LANGUAGE_VERSION_STRING);
	}

	public static void addDebugInfo(Minecraft minecraft, List<String> systemInfo) {
		if (minecraft.showOnlyReducedInfo()) {
			return;
		}

		systemInfo.add("");
		systemInfo.add("Flywheel: " + FlwImplXplat.INSTANCE.getVersionStr());
		systemInfo.add("Backend: " + BackendManagerImpl.getBackendString());
		systemInfo.add("Update limiting: " + (FlwConfig.INSTANCE.limitUpdates() ? "on" : "off"));

		VisualizationManager manager = VisualizationManager.get(minecraft.level);
		if (manager != null) {
			systemInfo.add("B: " + manager.blockEntities().visualCount()
					+ ", E: " + manager.entities().visualCount()
					+ ", F: " + manager.effects().visualCount());
			Vec3i renderOrigin = manager.renderOrigin();
			systemInfo.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
		}

		systemInfo.add("Memory Usage: CPU: " + StringUtil.formatBytes(FlwMemoryTracker.getCpuMemory()) + ", GPU: " + StringUtil.formatBytes(FlwMemoryTracker.getGpuMemory()));
	}
}
