layout(local_size_x = 8, local_size_y = 8) in;

layout(binding = 0, r32f) uniform writeonly image2D outImage;
layout(binding = 1) uniform sampler2D inImage;

uniform vec2 imageSize;
uniform int lod;

uniform int useMin = 0;

void main() {
    uvec2 pos = gl_GlobalInvocationID.xy;

    // Map the output texel to an input texel. Properly do the division because generating mip0 maps from the actual
    // full resolution depth buffer and the aspect ratio may be different from our Po2 pyramid.
    ivec2 samplePos = ivec2(floor(vec2(pos) * vec2(textureSize(inImage, lod)) / imageSize));

    float depth01 = texelFetchOffset(inImage, samplePos, lod, ivec2(0, 1)).r;
    float depth11 = texelFetchOffset(inImage, samplePos, lod, ivec2(1, 1)).r;
    float depth10 = texelFetchOffset(inImage, samplePos, lod, ivec2(1, 0)).r;
    float depth00 = texelFetchOffset(inImage, samplePos, lod, ivec2(0, 0)).r;

    float depth;
    if (useMin == 0) {
        depth = max(max(depth00, depth01), max(depth10, depth11));
    } else {
        depth = min(min(depth00, depth01), min(depth10, depth11));
    }

    imageStore(outImage, ivec2(pos), vec4(depth));
}
