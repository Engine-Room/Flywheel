package com.jozufozu.flywheel.backend.gl.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;

import net.minecraft.resources.ResourceLocation;

public class GlShader extends GlObject {

	public final ResourceLocation name;
	public final ShaderType type;

	public GlShader(ResourceLocation name, ShaderType type, String source) {
		this.name = name;
		this.type = type;
		int handle = GL20.glCreateShader(type.glEnum);

		GlCompat.safeShaderSource(handle, source);
		GL20.glCompileShader(handle);

//		File dir = new File(Minecraft.getInstance().gameDirectory, "flywheel_sources");
//		dir.mkdirs();
//		File file = new File(dir, name.toString().replaceAll("[:/]", "_"));
//		try (FileWriter writer = new FileWriter(file)) {
//			writer.write(source);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

//		String log = GL20.glGetShaderInfoLog(handle);
//
//		if (!log.isEmpty()) {
//			System.out.println(log);
////			env.printShaderInfoLog(source, log, this.name);
//		}

		if (GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE) {
			throw new ShaderLoadingException("Could not compile " + name + ". See log for details.");
		}

		setHandle(handle);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL20.glDeleteShader(handle);
	}

}
