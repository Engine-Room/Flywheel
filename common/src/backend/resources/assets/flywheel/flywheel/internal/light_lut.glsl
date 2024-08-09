const uint _FLW_BLOCKS_PER_SECTION = 18 * 18 * 18;
const uint _FLW_LIGHT_SIZE_BYTES = _FLW_BLOCKS_PER_SECTION;
const uint _FLW_SOLID_SIZE_BYTES = ((_FLW_BLOCKS_PER_SECTION + 31) / 32) * 4;
const uint _FLW_LIGHT_START_BYTES = _FLW_SOLID_SIZE_BYTES;
const uint _FLW_LIGHT_SECTION_SIZE_BYTES = _FLW_SOLID_SIZE_BYTES + _FLW_LIGHT_SIZE_BYTES;

const uint _FLW_SOLID_START_INTS = 0;
const uint _FLW_LIGHT_START_INTS = _FLW_SOLID_SIZE_BYTES / 4;
const uint _FLW_LIGHT_SECTION_SIZE_INTS = _FLW_LIGHT_SECTION_SIZE_BYTES / 4;

const uint _FLW_COMPLETELY_SOLID = 0x7FFFFFFu;
const float _FLW_EPSILON = 1e-5;

uint _flw_indexLut(uint index);

uint _flw_indexLight(uint index);

// Adding this option takes my test world from ~800 to ~1250 FPS on my 3060ti.
// I have not taken it to a profiler otherwise.
#pragma optionNV (unroll all)

/// Find the index for the next step in the LUT.
/// @param base The base index in the LUT, should point to the start of a coordinate span.
/// @param coord The coordinate to look for.
/// @param next Output. The index of the next step in the LUT.
/// @return true if the coordinate is not in the span.
bool _flw_nextLut(uint base, int coord, out uint next) {
    // The base coordinate.
    int start = int(_flw_indexLut(base));
    // The width of the coordinate span.
    uint size = _flw_indexLut(base + 1);

    // Index of the coordinate in the span.
    int i = coord - start;

    if (i < 0 || i >= size) {
        // We missed.
        return true;
    }

    next = _flw_indexLut(base + 2 + i);

    return false;
}

bool _flw_chunkCoordToSectionIndex(ivec3 sectionPos, out uint index) {
    uint y;
    if (_flw_nextLut(0, sectionPos.x, y) || y == 0) {
        return true;
    }

    uint z;
    if (_flw_nextLut(y, sectionPos.y, z) || z == 0) {
        return true;
    }

    uint sectionIndex;
    if (_flw_nextLut(z, sectionPos.z, sectionIndex) || sectionIndex == 0) {
        return true;
    }

    // The index is written as 1-based so we can properly detect missing sections.
    index = sectionIndex - 1;

    return false;
}

uvec2 _flw_lightAt(uint sectionOffset, uvec3 blockInSectionPos) {
    uint byteOffset = blockInSectionPos.x + blockInSectionPos.z * 18u + blockInSectionPos.y * 18u * 18u;

    uint uintOffset = byteOffset >> 2u;
    uint bitOffset = (byteOffset & 3u) << 3;

    uint raw = _flw_indexLight(sectionOffset + _FLW_LIGHT_START_INTS + uintOffset);
    uint block = (raw >> bitOffset) & 0xFu;
    uint sky = (raw >> (bitOffset + 4u)) & 0xFu;

    return uvec2(block, sky);
}

bool _flw_isSolid(uint sectionOffset, uvec3 blockInSectionPos) {
    uint bitOffset = blockInSectionPos.x + blockInSectionPos.z * 18u + blockInSectionPos.y * 18u * 18u;

    uint uintOffset = bitOffset >> 5u;
    uint bitInWordOffset = bitOffset & 31u;

    uint word = _flw_indexLight(sectionOffset + _FLW_SOLID_START_INTS + uintOffset);

    return (word & (1u << bitInWordOffset)) != 0;
}

bool flw_lightFetch(ivec3 blockPos, out vec2 lightCoord) {
    uint lightSectionIndex;
    if (_flw_chunkCoordToSectionIndex(blockPos >> 4, lightSectionIndex)) {
        return false;
    }
    // The offset of the section in the light buffer.
    uint sectionOffset = lightSectionIndex * _FLW_LIGHT_SECTION_SIZE_INTS;

    uvec3 blockInSectionPos = (blockPos & 0xF) + 1;

    lightCoord = vec2(_flw_lightAt(sectionOffset, blockInSectionPos)) / 15.;
    return true;
}

/// Premtively collect all light in a 3x3x3 area centered on our block.
/// Depending on the normal, we won't use all the data, but fetching on demand will have many duplicated fetches.
///
/// The output is a 3-component vector <blockLight, skyLight, valid ? 1 : 0> packed into a single uint to save
/// memory and ALU ops later on. 10 bits are used for each component. This allows 4 such packed ints to be added
/// together with room to spare before overflowing into the next component.
uint[27] _flw_fetchLight3x3x3(uint sectionOffset, ivec3 blockInSectionPos, uint solid) {
    uint[27] lights;

    uint index = 0u;
    uint mask = 1u;
    for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++) {
                // 0 if the block is solid, 1 if it's not.
                uint notSolid = uint((solid & mask) == 0u);
                uvec2 light = _flw_lightAt(sectionOffset, uvec3(blockInSectionPos + ivec3(x, y, z)));

                lights[index] = light.x;
                lights[index] |= (light.y) << 10;
                lights[index] |= (notSolid) << 20;

                index++;
                mask <<= 1;
            }
        }
    }

    return lights;
}

uint _flw_fetchSolid3x3x3(uint sectionOffset, ivec3 blockInSectionPos) {
    uint ret = 0;

    uint index = 0;
    for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++) {
                bool flag = _flw_isSolid(sectionOffset, uvec3(blockInSectionPos + ivec3(x, y, z)));
                ret |= uint(flag) << index;

                index++;
            }
        }
    }

    return ret;
}

#define _flw_index3x3x3(x, y, z) ((x) + (z) * 3u + (y) * 9u)
#define _flw_index3x3x3v(p) _flw_index3x3x3((p).x, (p).y, (p).z)
#define _flw_validCountToAo(validCount) (1. - (4. - (validCount)) * 0.2)

/// Calculate the light for a direction by averaging the light at the corners of the block.
///
/// To make this reusable across directions, c00..c11 choose what values relative to each corner to use.
/// e.g. (0, 0, 0) (0, 0, 1) (0, 1, 0) (0, 1, 1) would give you the light coming from -x at each corner.
/// In general, to get the light for a particular direction, you fix the x, y, or z coordinate of the c values, and permutate 0 and 1 for the other two.
/// Fixing the x coordinate to 0 gives you the light from -x, 1 gives you the light from +x.
///
/// @param lights The light data for the 3x3x3 area.
/// @param interpolant The position within the center block.
/// @param c00..c11 4 offsets to determine which "direction" we are averaging.
/// @param oppositeMask A bitmask telling this function which bit to flip to get the opposite index for a given corner
vec3 _flw_lightForDirection(uint[27] lights, vec3 interpolant, uvec3 c00, uvec3 c01, uvec3 c10, uvec3 c11, uint oppositeMask) {
    // Constant propatation should inline all of these index calculations,
    // but since they're distributive we can lay them out more nicely.
    uint ic00 = _flw_index3x3x3v(c00);
    uint ic01 = _flw_index3x3x3v(c01);
    uint ic10 = _flw_index3x3x3v(c10);
    uint ic11 = _flw_index3x3x3v(c11);

    const uint[8] corners = uint[](
    _flw_index3x3x3(0u, 0u, 0u),
    _flw_index3x3x3(0u, 0u, 1u),
    _flw_index3x3x3(0u, 1u, 0u),
    _flw_index3x3x3(0u, 1u, 1u),
    _flw_index3x3x3(1u, 0u, 0u),
    _flw_index3x3x3(1u, 0u, 1u),
    _flw_index3x3x3(1u, 1u, 0u),
    _flw_index3x3x3(1u, 1u, 1u)
    );

    // Division and branching are both kinda expensive, so use this table for the valid block normalization
    const float[5] normalizers = float[](0., 1., 1. / 2., 1. / 3., 1. / 4.);

    // Sum up the light and number of valid blocks in each corner for this direction
    uint[8] summed;
    for (uint i = 0; i < 8; i++) {
        uint corner = corners[i];
        summed[i] = lights[ic00 + corner] + lights[ic01 + corner] + lights[ic10 + corner] + lights[ic11 + corner];
    }

    // The final light and number of valid blocks for each corner.
    vec3[8] adjusted;
    for (uint i = 0; i < 8; i++) {
        #ifdef _FLW_INNER_FACE_CORRECTION
        // If the current corner has no valid blocks, use the opposite
        // corner's light based on which direction we're evaluating.
        // Because of how our corners are indexed, moving along one axis is the same as flipping a bit.
        uint cornerIndex = (summed[i] & 0xFFF00000u) == 0u ? i ^ oppositeMask : i;
        #else
        uint cornerIndex = i;
        #endif
        uint corner = summed[cornerIndex];

        uvec3 unpacked = uvec3(corner & 0x3FFu, (corner >> 10u) & 0x3FFu, corner >> 20u);

        // Normalize by the number of valid blocks.
        adjusted[i].xy = vec2(unpacked.xy) * normalizers[unpacked.z];
        adjusted[i].z = float(unpacked.z);
    }

    // Trilinear interpolation, including valid count
    vec3 light00 = mix(adjusted[0], adjusted[1], interpolant.z);
    vec3 light01 = mix(adjusted[2], adjusted[3], interpolant.z);
    vec3 light10 = mix(adjusted[4], adjusted[5], interpolant.z);
    vec3 light11 = mix(adjusted[6], adjusted[7], interpolant.z);

    vec3 light0 = mix(light00, light01, interpolant.y);
    vec3 light1 = mix(light10, light11, interpolant.y);

    vec3 light = mix(light0, light1, interpolant.x);

    // Normalize the light coords
    light.xy *= 1. / 15.;
    // Calculate the AO multiplier from the number of valid blocks
    light.z = _flw_validCountToAo(light.z);

    return light;
}

bool flw_light(vec3 worldPos, vec3 normal, out FlwLightAo light) {
    // Always use the section of the block we are contained in to ensure accuracy.
    // We don't want to interpolate between sections, but also we might not be able
    // to rely on the existence neighboring sections, so don't do any extra rounding here.
    ivec3 blockPos = ivec3(floor(worldPos)) + flw_renderOrigin;

    uint lightSectionIndex;
    if (_flw_chunkCoordToSectionIndex(blockPos >> 4, lightSectionIndex)) {
        return false;
    }
    // The offset of the section in the light buffer.
    uint sectionOffset = lightSectionIndex * _FLW_LIGHT_SECTION_SIZE_INTS;

    // The block's position in the section adjusted into 18x18x18 space
    ivec3 blockInSectionPos = (blockPos & 0xF) + 1;

    #if _FLW_LIGHT_SMOOTHNESS == 1// Directly trilerp as if sampling a texture

    // The lowest corner of the 2x2x2 area we'll be trilinear interpolating.
    // The ugly bit on the end evaluates to -1 or 0 depending on which side of 0.5 we are.
    uvec3 lowestCorner = blockInSectionPos + ivec3(floor(fract(worldPos) - 0.5));

    // The distance our fragment is from the center of the lowest corner.
    vec3 interpolant = fract(worldPos - 0.5);

    // Fetch everything for trilinear interpolation
    // Hypothetically we could re-order these and do some calculations in-between fetches
    // to help with latency hiding, but the compiler should be able to do that for us.
    vec2 light000 = vec2(_flw_lightAt(sectionOffset, lowestCorner));
    vec2 light100 = vec2(_flw_lightAt(sectionOffset, lowestCorner + uvec3(1, 0, 0)));
    vec2 light001 = vec2(_flw_lightAt(sectionOffset, lowestCorner + uvec3(0, 0, 1)));
    vec2 light101 = vec2(_flw_lightAt(sectionOffset, lowestCorner + uvec3(1, 0, 1)));
    vec2 light010 = vec2(_flw_lightAt(sectionOffset, lowestCorner + uvec3(0, 1, 0)));
    vec2 light110 = vec2(_flw_lightAt(sectionOffset, lowestCorner + uvec3(1, 1, 0)));
    vec2 light011 = vec2(_flw_lightAt(sectionOffset, lowestCorner + uvec3(0, 1, 1)));
    vec2 light111 = vec2(_flw_lightAt(sectionOffset, lowestCorner + uvec3(1, 1, 1)));

    vec2 light00 = mix(light000, light001, interpolant.z);
    vec2 light01 = mix(light010, light011, interpolant.z);
    vec2 light10 = mix(light100, light101, interpolant.z);
    vec2 light11 = mix(light110, light111, interpolant.z);

    vec2 light0 = mix(light00, light01, interpolant.y);
    vec2 light1 = mix(light10, light11, interpolant.y);

    light.light = mix(light0, light1, interpolant.x) / 15.;
    light.ao = 1.;

    #elif _FLW_LIGHT_SMOOTHNESS == 2// Lighting and AO accurate to chunk baking

    uint solid = _flw_fetchSolid3x3x3(sectionOffset, blockInSectionPos);

    if (solid == _FLW_COMPLETELY_SOLID) {
        // No point in doing any work if the entire 3x3x3 volume around us is filled.
        // Kinda rare but this may happen if our fragment is in the middle of a lot of tinted glass
        light.light = vec2(0.);
        light.ao = _flw_validCountToAo(0.);
        return true;
    }

    // Fetch everything in a 3x3x3 area centered around the block.
    uint[27] lights = _flw_fetchLight3x3x3(sectionOffset, blockInSectionPos, solid);

    vec3 interpolant = fract(worldPos);

    // Average the light in relevant directions at each corner, skipping directions that would have no influence

    vec3 lightX;
    if (normal.x > _FLW_EPSILON) {
        lightX = _flw_lightForDirection(lights, interpolant, uvec3(1u, 0u, 0u), uvec3(1u, 0u, 1u), uvec3(1u, 1u, 0u), uvec3(1u, 1u, 1u), 4u);
    } else if (normal.x < -_FLW_EPSILON) {
        lightX = _flw_lightForDirection(lights, interpolant, uvec3(0u, 0u, 0u), uvec3(0u, 0u, 1u), uvec3(0u, 1u, 0u), uvec3(0u, 1u, 1u), 4u);
    } else {
        lightX = vec3(0.);
    }

    vec3 lightZ;
    if (normal.z > _FLW_EPSILON) {
        lightZ = _flw_lightForDirection(lights, interpolant, uvec3(0u, 0u, 1u), uvec3(0u, 1u, 1u), uvec3(1u, 0u, 1u), uvec3(1u, 1u, 1u), 1u);
    } else if (normal.z < -_FLW_EPSILON) {
        lightZ = _flw_lightForDirection(lights, interpolant, uvec3(0u, 0u, 0u), uvec3(0u, 1u, 0u), uvec3(1u, 0u, 0u), uvec3(1u, 1u, 0u), 1u);
    } else {
        lightZ = vec3(0.);
    }

    vec3 lightY;
    if (normal.y > _FLW_EPSILON) {
        lightY = _flw_lightForDirection(lights, interpolant, uvec3(0u, 1u, 0u), uvec3(0u, 1u, 1u), uvec3(1u, 1u, 0u), uvec3(1u, 1u, 1u), 2u);
    } else if (normal.y < -_FLW_EPSILON) {
        lightY = _flw_lightForDirection(lights, interpolant, uvec3(0u, 0u, 0u), uvec3(0u, 0u, 1u), uvec3(1u, 0u, 0u), uvec3(1u, 0u, 1u), 2u);
    } else {
        lightY = vec3(0.);
    }

    vec3 n2 = normal * normal;
    vec3 lightAo = lightX * n2.x + lightY * n2.y + lightZ * n2.z;

    light.light = lightAo.xy;
    light.ao = lightAo.z;

    #else// Entirely flat lighting, the lowest setting and a fallback in case an invalid option is set

    light.light = vec2(_flw_lightAt(sectionOffset, blockInSectionPos)) / 15.;
    light.ao = 1.;

    #endif

    return true;
}

