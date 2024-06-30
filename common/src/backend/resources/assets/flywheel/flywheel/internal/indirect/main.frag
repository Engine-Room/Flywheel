#include "flywheel:internal/common.frag"
#include "flywheel:internal/indirect/buffer_bindings.glsl"

flat in uvec3 _flw_packedMaterial;

#ifdef _FLW_EMBEDDED

layout(std430, binding = _FLW_EMBEDDING_LUT_BINDING) restrict readonly buffer EmbeddingLut {
    uint _flw_embeddingLut[];
};

const uint _FLW_LIGHT_SECTION_SIZE_BYTES = 18 * 18 * 18;
const uint _FLW_LIGHT_SECTION_SIZE_INTS = _FLW_LIGHT_SECTION_SIZE_BYTES / 4;

layout(std430, binding = _FLW_EMBEDDING_LIGHT_BINDING) restrict readonly buffer LightSections {
    uint _flw_lightSections[];
};

/// Find the index for the next step in the LUT.
/// @param base The base index in the LUT, should point to the start of a coordinate span.
/// @param coord The coordinate to look for.
/// @param next Output. The index of the next step in the LUT.
/// @return true if the coordinate is not in the span.
bool _flw_nextLut(uint base, int coord, out uint next) {
    // The base coordinate.
    int start = int(_flw_embeddingLut[base]);
    // The width of the coordinate span.
    uint size = _flw_embeddingLut[base + 1];

    // Index of the coordinate in the span.
    int i = coord - start;

    if (i < 0 || i >= size) {
        // We missed.
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

vec2 _flw_lightAt(uint sectionOffset, uvec3 blockInSectionPos) {
    uint byteOffset = blockInSectionPos.x + blockInSectionPos.z * 18u + blockInSectionPos.y * 18u * 18u;

    uint uintOffset = byteOffset >> 2u;
    uint bitOffset = (byteOffset & 3u) << 3;

    uint raw = _flw_lightSections[sectionOffset + uintOffset];
    uint block = (raw >> bitOffset) & 0xFu;
    uint sky = (raw >> (bitOffset + 4u)) & 0xFu;

    return vec2(block / 15., sky / 15.);
}

bool _flw_embeddedLight(vec3 worldPos, out vec2 lightCoord) {
    ivec3 blockPos = ivec3(floor(worldPos));

    ivec3 sectionPos = blockPos >> 4;
    uvec3 blockInSectionPos = (blockPos & 0xF) + 1;

    uint lightSectionIndex;
    if (_flw_chunkCoordToSectionIndex(sectionPos, lightSectionIndex)) {
        // TODO: useful debug mode for this.
        // flw_fragOverlay = ivec2(0, 3);
        return false;
    }

    uint sectionOffset = lightSectionIndex * _FLW_LIGHT_SECTION_SIZE_INTS;

    lightCoord = _flw_lightAt(sectionOffset, blockInSectionPos);
    return true;
}

#endif

void main() {
    _flw_uberMaterialFragmentIndex = _flw_packedMaterial.x;
    _flw_unpackUint2x16(_flw_packedMaterial.y, _flw_uberCutoutIndex, _flw_uberFogIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.z, flw_material);

    _flw_main();
}
