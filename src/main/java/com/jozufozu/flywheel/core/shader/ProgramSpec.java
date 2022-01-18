package com.jozufozu.flywheel.core.shader;

import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.Resolver;
import com.jozufozu.flywheel.core.source.SourceFile;
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

	public static final Codec<ProgramSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("vertex")
					.forGetter(ProgramSpec::getSourceLoc),
				ResourceLocation.CODEC.fieldOf("fragment")
					.forGetter(ProgramSpec::getFragmentLoc))
			.apply(instance, ProgramSpec::new));

	public ResourceLocation name;
	public final FileResolution vertex;
	public final FileResolution fragment;

	public ProgramSpec(ResourceLocation vertex, ResourceLocation fragment) {
		this.vertex = Resolver.INSTANCE.get(vertex);
		this.fragment = Resolver.INSTANCE.get(fragment);
	}

	public void setName(ResourceLocation name) {
		this.name = name;
		this.vertex.addSpec(name);
		this.fragment.addSpec(name);
	}

	public ResourceLocation getSourceLoc() {
		return vertex.getFileLoc();
	}

	public ResourceLocation getFragmentLoc() {
		return fragment.getFileLoc();
	}

	public SourceFile getVertexFile() {
		return vertex.getFile();
	}

	public SourceFile getFragmentFile() {
		return fragment.getFile();
	}

	@Override
	public String toString() {
		return name.toString();
	}
}
