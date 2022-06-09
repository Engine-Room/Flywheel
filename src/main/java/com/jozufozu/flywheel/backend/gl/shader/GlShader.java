package com.jozufozu.flywheel.backend.gl.shader;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.core.compile.ShaderCompilationException;
import com.jozufozu.flywheel.core.shader.ShaderConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class GlShader extends GlObject {

	public final ShaderType type;
	private final List<ResourceLocation> parts;
	private final ShaderConstants constants;

	public GlShader(String source, ShaderType type, List<ResourceLocation> parts, ShaderConstants constants) {
		this.parts = parts;
		this.type = type;
		this.constants = constants;
		int handle = GL20.glCreateShader(type.glEnum);

		GlCompat.safeShaderSource(handle, source);
		GL20.glCompileShader(handle);

		dumpSource(source, type);

		if (GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE) {
			throw new ShaderCompilationException("Could not compile " + getName(), handle);
		}

		setHandle(handle);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL20.glDeleteShader(handle);
	}

	public String getName() {
		return parts.stream()
				.map(ResourceLocation::toString)
				.map(s -> s.replaceAll("/", "_")
						.replaceAll(":", "\\$"))
				.collect(Collectors.joining(";")) + ';' + Integer.toHexString(constants.hashCode());
	}

	private void dumpSource(String source, ShaderType type) {
		if (!Backend.DUMP_SHADER_SOURCE) {
			return;
		}

		File dir = new File(Minecraft.getInstance().gameDirectory, "flywheel_sources");
		dir.mkdirs();
		File file = new File(dir, type.getFileName(getName()));
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(source);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
