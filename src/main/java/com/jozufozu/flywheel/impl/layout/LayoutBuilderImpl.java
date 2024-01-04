package com.jozufozu.flywheel.impl.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.Range;

import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.layout.Layout.Element;
import com.jozufozu.flywheel.api.layout.LayoutBuilder;
import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.impl.layout.LayoutImpl.ElementImpl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class LayoutBuilderImpl implements LayoutBuilder {
	private static final Set<String> GLSL_KEYWORDS = Set.of(
			// standard keywords
			"const", "uniform", "buffer", "shared", "attribute", "varying",
			"coherent", "volatile", "restrict", "readonly", "writeonly",
			"atomic_uint",
			"layout",
			"centroid", "flat", "smooth", "noperspective",
			"patch", "sample",
			"invariant", "precise",
			"break", "continue", "do", "for", "while", "switch", "case", "default",
			"if", "else",
			"subroutine",
			"in", "out", "inout",
			"int", "void", "bool", "true", "false", "float", "double",
			"discard", "return",
			"vec2", "vec3", "vec4", "ivec2", "ivec3", "ivec4", "bvec2", "bvec3", "bvec4",
			"uint", "uvec2", "uvec3", "uvec4",
			"dvec2", "dvec3", "dvec4",
			"mat2", "mat3", "mat4",
			"mat2x2", "mat2x3", "mat2x4",
			"mat3x2", "mat3x3", "mat3x4",
			"mat4x2", "mat4x3", "mat4x4",
			"dmat2", "dmat3", "dmat4",
			"dmat2x2", "dmat2x3", "dmat2x4",
			"dmat3x2", "dmat3x3", "dmat3x4",
			"dmat4x2", "dmat4x3", "dmat4x4",
			"lowp", "mediump", "highp", "precision",
			"sampler1D", "sampler1DShadow", "sampler1DArray", "sampler1DArrayShadow",
			"isampler1D", "isampler1DArray", "usampler1D", "usampler1DArray",
			"sampler2D", "sampler2DShadow", "sampler2DArray", "sampler2DArrayShadow",
			"isampler2D", "isampler2DArray", "usampler2D", "usampler2DArray",
			"sampler2DRect", "sampler2DRectShadow", "isampler2DRect", "usampler2DRect",
			"sampler2DMS", "isampler2DMS", "usampler2DMS",
			"sampler2DMSArray", "isampler2DMSArray", "usampler2DMSArray",
			"sampler3D", "isampler3D", "usampler3D",
			"samplerCube", "samplerCubeShadow", "isamplerCube", "usamplerCube",
			"samplerCubeArray", "samplerCubeArrayShadow",
			"isamplerCubeArray", "usamplerCubeArray",
			"samplerBuffer", "isamplerBuffer", "usamplerBuffer",
			"image1D", "iimage1D", "uimage1D",
			"image1DArray", "iimage1DArray", "uimage1DArray",
			"image2D", "iimage2D", "uimage2D",
			"image2DArray", "iimage2DArray", "uimage2DArray",
			"image2DRect", "iimage2DRect", "uimage2DRect",
			"image2DMS", "iimage2DMS", "uimage2DMS",
			"image2DMSArray", "iimage2DMSArray", "uimage2DMSArray",
			"image3D", "iimage3D", "uimage3D",
			"imageCube", "iimageCube", "uimageCube",
			"imageCubeArray", "iimageCubeArray", "uimageCubeArray",
			"imageBuffer", "iimageBuffer", "uimageBuffer",
			"struct",
			// Vulkan keywords
			"texture1D", "texture1DArray",
			"itexture1D", "itexture1DArray", "utexture1D", "utexture1DArray",
			"texture2D", "texture2DArray",
			"itexture2D", "itexture2DArray", "utexture2D", "utexture2DArray",
			"texture2DRect", "itexture2DRect", "utexture2DRect",
			"texture2DMS", "itexture2DMS", "utexture2DMS",
			"texture2DMSArray", "itexture2DMSArray", "utexture2DMSArray",
			"texture3D", "itexture3D", "utexture3D",
			"textureCube", "itextureCube", "utextureCube",
			"textureCubeArray", "itextureCubeArray", "utextureCubeArray",
			"textureBuffer", "itextureBuffer", "utextureBuffer",
			"sampler", "samplerShadow",
			"subpassInput", "isubpassInput", "usubpassInput",
			"subpassInputMS", "isubpassInputMS", "usubpassInputMS",
			// reserved keywords
			"common", "partition", "active",
			"asm",
			"class", "union", "enum", "typedef", "template", "this",
			"resource",
			"goto",
			"inline", "noinline", "public", "static", "extern", "external", "interface",
			"long", "short", "half", "fixed", "unsigned", "superp",
			"input", "output",
			"hvec2", "hvec3", "hvec4", "fvec2", "fvec3", "fvec4",
			"filter",
			"sizeof", "cast",
			"namespace", "using",
			"sampler3DRect"
	);

	private final List<Element> elements = new ArrayList<>();

	@Override
	public LayoutBuilder scalar(String name, ValueRepr repr) {
		elements.add(new ElementImpl(name, new ScalarElementTypeImpl(repr)));
		return this;
	}

	@Override
	public LayoutBuilder vector(String name, ValueRepr repr, @Range(from = 2, to = 4) int size) {
		elements.add(new ElementImpl(name, new VectorElementTypeImpl(repr, size)));
		return this;
	}

	@Override
	public LayoutBuilder matrix(String name, FloatRepr repr, @Range(from = 2, to = 4) int rows, @Range(from = 2, to = 4) int columns) {
		elements.add(new ElementImpl(name, new MatrixElementTypeImpl(repr, rows, columns)));
		return this;
	}

	@Override
	public LayoutBuilder matrix(String name, FloatRepr repr, @Range(from = 2, to = 4) int size) {
		return matrix(name, repr, size, size);
	}

	@Override
	public Layout build() {
		Object2IntMap<String> name2IndexMap = new Object2IntOpenHashMap<>();
		name2IndexMap.defaultReturnValue(-1);

		for (int i = 0; i < elements.size(); i++) {
			Element element = elements.get(i);
			String name = element.name();

			if (GLSL_KEYWORDS.contains(name)) {
				throw new IllegalStateException("Element at index " + i + " has invalid name '" + name + "'; this is a GLSL keyword!");
			}

			for (int j = 0; j < name.length(); j++) {
				char c = name.charAt(j);
				if (j == 0) {
					if (!isLetter(c)) {
						throw new IllegalStateException("Element at index " + i + " has invalid name '" + name + "'! Names must start with a letter.");
					}
				} else {
					if (!isValidNameCharacter(c)) {
						throw new IllegalStateException("Element at index " + i + " has invalid name '" + name + "'! Names must only contain letters, digits, and underscores.");
					}
				}
			}

			String lowerCaseName = name.toLowerCase(Locale.ROOT);
			if (lowerCaseName.startsWith("gl_") || lowerCaseName.startsWith("flw_")) {
				throw new IllegalStateException("Element at index " + i + " has invalid name '" + name + "'! Names must not start with 'gl_' or 'flw_' (case-insensitive).");
			}

			if (name.length() > Layout.MAX_ELEMENT_NAME_LENGTH) {
				throw new IllegalStateException("Element at index " + i + " has invalid name '" + name + "'! Names must not be longer than " + Layout.MAX_ELEMENT_NAME_LENGTH + " characters.");
			}

			int prevIndex = name2IndexMap.putIfAbsent(name, i);
			if (prevIndex != -1) {
				throw new IllegalStateException("Elements at indices " + prevIndex + " and " + i + " have the same name; this is not valid!");
			}
		}

		return new LayoutImpl(List.copyOf(elements));
	}

	private static boolean isValidNameCharacter(char c) {
		return isLetter(c) || c >= '0' && c <= '9' || c == '_';
	}

	private static boolean isLetter(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}
}
