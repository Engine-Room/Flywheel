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

const uint _FLW_LOWER_10_BITS = 0x3FFu;
const uint _FLW_UPPER_10_BITS = 0xFFF00000u;

uint _flw_indexLut(uint index);

uint _flw_indexLight(uint index);

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


uint _flw_fetchSolid3x3x3(uint sectionOffset, ivec3 blockInSectionPos) {
    uint ret = 0;

    // The formatter does NOT like these macros
    // @formatter:off

    #define _FLW_FETCH_SOLID(x, y, z, i) { \
        bool flag = _flw_isSolid(sectionOffset, uvec3(blockInSectionPos + ivec3(x, y, z))); \
        ret |= uint(flag) << i; \
    }

    /// fori y, z, x: unrolled
    _FLW_FETCH_SOLID(-1, -1, -1, 0)
    _FLW_FETCH_SOLID(0, -1, -1, 1)
    _FLW_FETCH_SOLID(1, -1, -1, 2)

    _FLW_FETCH_SOLID(-1, -1, 0, 3)
    _FLW_FETCH_SOLID(0, -1, 0, 4)
    _FLW_FETCH_SOLID(1, -1, 0, 5)

    _FLW_FETCH_SOLID(-1, -1, 1, 6)
    _FLW_FETCH_SOLID(0, -1, 1, 7)
    _FLW_FETCH_SOLID(1, -1, 1, 8)

    _FLW_FETCH_SOLID(-1, 0, -1, 9)
    _FLW_FETCH_SOLID(0, 0, -1, 10)
    _FLW_FETCH_SOLID(1, 0, -1, 11)

    _FLW_FETCH_SOLID(-1, 0, 0, 12)
    _FLW_FETCH_SOLID(0, 0, 0, 13)
    _FLW_FETCH_SOLID(1, 0, 0, 14)

    _FLW_FETCH_SOLID(-1, 0, 1, 15)
    _FLW_FETCH_SOLID(0, 0, 1, 16)
    _FLW_FETCH_SOLID(1, 0, 1, 17)

    _FLW_FETCH_SOLID(-1, 1, -1, 18)
    _FLW_FETCH_SOLID(0, 1, -1, 19)
    _FLW_FETCH_SOLID(1, 1, -1, 20)

    _FLW_FETCH_SOLID(-1, 1, 0, 21)
    _FLW_FETCH_SOLID(0, 1, 0, 22)
    _FLW_FETCH_SOLID(1, 1, 0, 23)

    _FLW_FETCH_SOLID(-1, 1, 1, 24)
    _FLW_FETCH_SOLID(0, 1, 1, 25)
    _FLW_FETCH_SOLID(1, 1, 1, 26)

    // @formatter:on

    return ret;
}

/// Premtively collect all light in a 3x3x3 area centered on our block.
/// Depending on the normal, we won't use all the data, but fetching on demand will have many duplicated fetches.
/// Only fetching what we'll actually use using a bitmask turned out significantly slower, but perhaps a less
/// granular approach could see wins.
///
/// The output is a 3-component vector <blockLight, skyLight, valid ? 1 : 0> packed into a single uint to save
/// memory and ALU ops later on. 10 bits are used for each component. This allows 4 such packed ints to be added
/// together with room to spare before overflowing into the next component.
uint[27] _flw_fetchLight3x3x3(uint sectionOffset, ivec3 blockInSectionPos, uint solidMask) {
    uint[27] lights;

    // @formatter:off
    #define _FLW_FETCH_LIGHT(_x, _y, _z, i) { \
        uvec2 light = _flw_lightAt(sectionOffset, uvec3(blockInSectionPos + ivec3(_x, _y, _z))); \
        lights[i] = (light.x) | ((light.y) << 10) | (uint((solidMask & (1u << i)) == 0u) << 20); \
    }

    /// fori y, z, x: unrolled
    _FLW_FETCH_LIGHT(-1, -1, -1, 0)
    _FLW_FETCH_LIGHT(0, -1, -1, 1)
    _FLW_FETCH_LIGHT(1, -1, -1, 2)

    _FLW_FETCH_LIGHT(-1, -1, 0, 3)
    _FLW_FETCH_LIGHT(0, -1, 0, 4)
    _FLW_FETCH_LIGHT(1, -1, 0, 5)

    _FLW_FETCH_LIGHT(-1, -1, 1, 6)
    _FLW_FETCH_LIGHT(0, -1, 1, 7)
    _FLW_FETCH_LIGHT(1, -1, 1, 8)

    _FLW_FETCH_LIGHT(-1, 0, -1, 9)
    _FLW_FETCH_LIGHT(0, 0, -1, 10)
    _FLW_FETCH_LIGHT(1, 0, -1, 11)

    _FLW_FETCH_LIGHT(-1, 0, 0, 12)
    _FLW_FETCH_LIGHT(0, 0, 0, 13)
    _FLW_FETCH_LIGHT(1, 0, 0, 14)

    _FLW_FETCH_LIGHT(-1, 0, 1, 15)
    _FLW_FETCH_LIGHT(0, 0, 1, 16)
    _FLW_FETCH_LIGHT(1, 0, 1, 17)

    _FLW_FETCH_LIGHT(-1, 1, -1, 18)
    _FLW_FETCH_LIGHT(0, 1, -1, 19)
    _FLW_FETCH_LIGHT(1, 1, -1, 20)

    _FLW_FETCH_LIGHT(-1, 1, 0, 21)
    _FLW_FETCH_LIGHT(0, 1, 0, 22)
    _FLW_FETCH_LIGHT(1, 1, 0, 23)

    _FLW_FETCH_LIGHT(-1, 1, 1, 24)
    _FLW_FETCH_LIGHT(0, 1, 1, 25)
    _FLW_FETCH_LIGHT(1, 1, 1, 26)

    // @formatter:on

    return lights;
}

#define _flw_index3x3x3(x, y, z) ((x) + (z) * 3u + (y) * 9u)
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
vec3 _flw_lightForDirection(uint[27] lights, vec3 interpolant, uint c00, uint c01, uint c10, uint c11, uint oppositeMask) {
    // Sum up the light and number of valid blocks in each corner for this direction
    uint[8] summed;

    // @formatter:off

    #define _FLW_SUM_CORNER(_x, _y, _z, i) { \
        const uint corner = _flw_index3x3x3(_x, _y, _z); \
        summed[i] = lights[c00 + corner] + lights[c01 + corner] + lights[c10 + corner] + lights[c11 + corner]; \
    }

    _FLW_SUM_CORNER(0u, 0u, 0u, 0)
    _FLW_SUM_CORNER(1u, 0u, 0u, 1)
    _FLW_SUM_CORNER(0u, 0u, 1u, 2)
    _FLW_SUM_CORNER(1u, 0u, 1u, 3)
    _FLW_SUM_CORNER(0u, 1u, 0u, 4)
    _FLW_SUM_CORNER(1u, 1u, 0u, 5)
    _FLW_SUM_CORNER(0u, 1u, 1u, 6)
    _FLW_SUM_CORNER(1u, 1u, 1u, 7)

    // @formatter:on

    // The final light and number of valid blocks for each corner.
    vec3[8] adjusted;

    #ifdef _FLW_INNER_FACE_CORRECTION
    // If the current corner has no valid blocks, use the opposite
    // corner's light based on which direction we're evaluating.
    // Because of how our corners are indexed, moving along one axis is the same as flipping a bit.
    #define _FLW_CORNER_INDEX(i) ((summed[i] & _FLW_UPPER_10_BITS) == 0u ? i ^ oppositeMask : i)
    #else
    #define _FLW_CORNER_INDEX(i) i
    #endif

    // Division and branching (to avoid dividing by zero) are both kinda expensive, so use this table for the valid block normalization
    const float[5] normalizers = float[](0., 1., 1. / 2., 1. / 3., 1. / 4.);

    // @formatter:off

    #define _FLW_ADJUST_CORNER(i) { \
        uint corner = summed[_FLW_CORNER_INDEX(i)]; \
        uint validCount = corner >> 20u; \
        adjusted[i].xy = vec2(corner & _FLW_LOWER_10_BITS, (corner >> 10u) & _FLW_LOWER_10_BITS) * normalizers[validCount]; \
        adjusted[i].z = float(validCount); \
    }

    _FLW_ADJUST_CORNER(0)
    _FLW_ADJUST_CORNER(1)
    _FLW_ADJUST_CORNER(2)
    _FLW_ADJUST_CORNER(3)
    _FLW_ADJUST_CORNER(4)
    _FLW_ADJUST_CORNER(5)
    _FLW_ADJUST_CORNER(6)
    _FLW_ADJUST_CORNER(7)

    // @formatter:on

    // Trilinear interpolation, including valid count
    vec3 light00 = mix(adjusted[0], adjusted[1], interpolant.x);
    vec3 light01 = mix(adjusted[2], adjusted[3], interpolant.x);
    vec3 light10 = mix(adjusted[4], adjusted[5], interpolant.x);
    vec3 light11 = mix(adjusted[6], adjusted[7], interpolant.x);

    vec3 light0 = mix(light00, light01, interpolant.z);
    vec3 light1 = mix(light10, light11, interpolant.z);

    vec3 light = mix(light0, light1, interpolant.y);

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

    // Directly trilerp as if sampling a texture
    #if _FLW_LIGHT_SMOOTHNESS == 1

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

    // Lighting and AO accurate to chunk baking
    #elif _FLW_LIGHT_SMOOTHNESS == 2

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
        lightX = _flw_lightForDirection(lights, interpolant, _flw_index3x3x3(1u, 0u, 0u), _flw_index3x3x3(1u, 0u, 1u), _flw_index3x3x3(1u, 1u, 0u), _flw_index3x3x3(1u, 1u, 1u), 1u);
    } else if (normal.x < -_FLW_EPSILON) {
        lightX = _flw_lightForDirection(lights, interpolant, _flw_index3x3x3(0u, 0u, 0u), _flw_index3x3x3(0u, 0u, 1u), _flw_index3x3x3(0u, 1u, 0u), _flw_index3x3x3(0u, 1u, 1u), 1u);
    } else {
        lightX = vec3(0.);
    }

    vec3 lightZ;
    if (normal.z > _FLW_EPSILON) {
        lightZ = _flw_lightForDirection(lights, interpolant, _flw_index3x3x3(0u, 0u, 1u), _flw_index3x3x3(0u, 1u, 1u), _flw_index3x3x3(1u, 0u, 1u), _flw_index3x3x3(1u, 1u, 1u), 2u);
    } else if (normal.z < -_FLW_EPSILON) {
        lightZ = _flw_lightForDirection(lights, interpolant, _flw_index3x3x3(0u, 0u, 0u), _flw_index3x3x3(0u, 1u, 0u), _flw_index3x3x3(1u, 0u, 0u), _flw_index3x3x3(1u, 1u, 0u), 2u);
    } else {
        lightZ = vec3(0.);
    }

    vec3 lightY;
    if (normal.y > _FLW_EPSILON) {
        lightY = _flw_lightForDirection(lights, interpolant, _flw_index3x3x3(0u, 1u, 0u), _flw_index3x3x3(0u, 1u, 1u), _flw_index3x3x3(1u, 1u, 0u), _flw_index3x3x3(1u, 1u, 1u), 4u);
    } else if (normal.y < -_FLW_EPSILON) {
        lightY = _flw_lightForDirection(lights, interpolant, _flw_index3x3x3(0u, 0u, 0u), _flw_index3x3x3(0u, 0u, 1u), _flw_index3x3x3(1u, 0u, 0u), _flw_index3x3x3(1u, 0u, 1u), 4u);
    } else {
        lightY = vec3(0.);
    }

    vec3 n2 = normal * normal;
    vec3 lightAo = lightX * n2.x + lightY * n2.y + lightZ * n2.z;

    light.light = lightAo.xy;
    light.ao = lightAo.z;

    // Entirely flat lighting, the lowest setting and a fallback in case an invalid option is set
    #else

    light.light = vec2(_flw_lightAt(sectionOffset, blockInSectionPos)) / 15.;
    light.ao = 1.;

    #endif

    return true;
}

