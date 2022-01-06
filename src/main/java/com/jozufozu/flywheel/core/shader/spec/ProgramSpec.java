package com.jozufozu.flywheel.core.shader.spec;

import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.Resolver;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

/**
 * An object describing a shader program that can be loaded by flywheel.
 *
 * <p>
 *     These are defined through json. All ProgramSpecs in {@code assets/modid/flywheel/programs} are parsed and
 *     processed. One ProgramSpec typically specifies one "material" that can be used in game to render things.
 * </p>
 * <p>
 *     All shader source files in {@code assets/modid/flywheel/shaders} are completely loaded and parsed into
 *     {@link SourceFile SourceFiles}, but not compiled until one of them is
 *     referenced by a ProgramSpec.
 * </p>
 */
public class ProgramSpec {

	// TODO: Block model style inheritance?
	public static final Codec<ProgramSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("source")
					.forGetter(ProgramSpec::getSourceLoc),
			ProgramState.CODEC.listOf()
					.optionalFieldOf("states", Collections.emptyList())
					.forGetter(ProgramSpec::getStates))
			.apply(instance, ProgramSpec::new));

	public ResourceLocation name;
	public final FileResolution source;

	public final List<ProgramState> states;

	public ProgramSpec(ResourceLocation source, List<ProgramState> states) {
		this.source = Resolver.INSTANCE.findShader(source);
		this.states = states;
	}

	public void setName(ResourceLocation name) {
		this.name = name;
	}

	public ResourceLocation getSourceLoc() {
		return source.getFileLoc();
	}

	public FileResolution getSource() {
		return source;
	}

	public List<ProgramState> getStates() {
		return states;
	}

}
