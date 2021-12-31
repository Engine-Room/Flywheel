package com.jozufozu.flywheel.core.shader.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
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
 *     These are defined through json. All ProgramSpecs in <code>assets/modid/flywheel/programs</code> are parsed and
 *     processed. One ProgramSpec typically specifies one "material" that can be used in game to render things.
 * </p>
 * <p>
 *     All shader source files in <code>assets/modid/flywheel/shaders</code> are completely loaded and parsed into
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

	public final ImmutableList<ProgramState> states;

	public ProgramSpec(ResourceLocation source, List<ProgramState> states) {
		this.source = Resolver.INSTANCE.findShader(source);
		this.states = ImmutableList.copyOf(states);
	}

	public void setName(ResourceLocation name) {
		this.name = name;
	}

	public ResourceLocation getSourceLoc() {
		return source.getFileLoc();
	}

	public SourceFile getSource() {
		return source.getFile();
	}

	public ImmutableList<ProgramState> getStates() {
		return states;
	}

	/**
	 * Calculate a unique ID representing the current game state.
	 */
    public long getCurrentStateID() {
        long ctx = 0;
        for (ProgramState state : states) {
            if (state.context().isTrue()) {
                ctx |= 1;
            }
            ctx <<= 1;
        }
        return ctx;
    }

	/**
	 * Given the stateID, get a list of defines to include at the top of a compiling program.
	 */
	public List<String> getDefines(long stateID) {
		List<String> defines = new ArrayList<>();

		for (ProgramState state : states) {
			if ((stateID & 1) == 1) {
				defines.addAll(state.defines());
			}
			stateID >>= 1;
		}
		return defines;
	}
}
