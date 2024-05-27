#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/indirect/buffer_bindings.glsl"
#include "flywheel:internal/indirect/draw_command.glsl"

layout(std430, binding = _FLW_TARGET_BUFFER_BINDING) restrict readonly buffer TargetBuffer {
    uint _flw_instanceIndices[];
};

layout(std430, binding = _FLW_DRAW_BUFFER_BINDING) restrict readonly buffer DrawBuffer {
    MeshDrawCommand _flw_drawCommands[];
};

#ifdef _FLW_EMBEDDED

layout(std430, binding = 8) restrict readonly buffer EmbeddingLut {
    uint _flw_embeddingLut[];
};

const uint _FLW_LIGHT_SECTION_SIZE_BYTES = 18 * 18 * 18;
const uint _FLW_LIGHT_SECTION_SIZE_INTS = _FLW_LIGHT_SECTION_SIZE_BYTES / 4;

layout(std430, binding = 9) restrict readonly buffer LightSections {
    uint _flw_lightSections[];
};

bool _flw_nextLut(uint base, int coord, out uint next) {
    int start = int(_flw_embeddingLut[base]);
    uint size = _flw_embeddingLut[base + 1];

    int i = coord - start;

    if (i < 0 || i >= size) {
        return true;
    }

    next = _flw_embeddingLut[base + 2 + i];

    return false;
}

bool _flw_chunkCoordToSectionIndex(ivec3 sectionPos, out uint index) {
    uint y;
    if (_flw_nextLut(0, sectionPos.x, y)) {
        return true;
    }

    uint z;
    if (_flw_nextLut(y, sectionPos.y, z)) {
        return true;
    }
    return _flw_nextLut(z, sectionPos.z, index);
}

vec2 _flw_lightAt(uint sectionOffset, ivec3 blockInSectionPos) {
    uint byteOffset = blockInSectionPos.x + blockInSectionPos.z * 18u + blockInSectionPos.y * 18u * 18u;

    uint uintOffset = byteOffset >> 2u;
    uint bitOffset = (byteOffset & 3u) << 3;

    uint packed = _flw_lightSections[sectionOffset + uintOffset];
    uint block = (packed >> bitOffset) & 0xFu;
    uint sky = (packed >> (bitOffset + 4u)) & 0xFu;

    return vec2(block, sky);
}

bool _flw_embeddedLight(vec3 worldPos, vec3 normal, out vec2 lightCoord) {
    ivec3 sectionPos = blockPos >> 4;
    uvec3 blockInSectionPos = (blockPos & 0xF) + 1;

    uint lightSectionIndex;
    if (_flw_chunkCoordToSectionIndex(sectionPos, lightSectionIndex)) {
        return false;
    }

    uint sectionOffset = lightSectionIndex * _FLW_LIGHT_SECTION_SIZE_INTS;

    lightCoord = _flw_lightAt(sectionOffset, blockInSectionPos);
    return true;
}

#endif

uniform uint _flw_baseDraw;

flat out uvec3 _flw_packedMaterial;

void main() {
#if __VERSION__ < 460
    uint drawIndex = gl_DrawIDARB + _flw_baseDraw;
#else
    uint drawIndex = gl_DrawID + _flw_baseDraw;
#endif
    MeshDrawCommand draw = _flw_drawCommands[drawIndex];

    _flw_uberMaterialVertexIndex = draw.materialVertexIndex;
    uint packedMaterialProperties = draw.packedMaterialProperties;
    _flw_unpackMaterialProperties(packedMaterialProperties, flw_material);
    _flw_packedMaterial = uvec3(draw.materialFragmentIndex, draw.packedFogAndCutout, packedMaterialProperties);

#if __VERSION__ < 460
    uint instanceIndex = _flw_instanceIndices[gl_BaseInstanceARB + gl_InstanceID];
#else
    uint instanceIndex = _flw_instanceIndices[gl_BaseInstance + gl_InstanceID];
#endif
    FlwInstance instance = _flw_unpackInstance(instanceIndex);

    _flw_main(instance, instanceIndex);
}
