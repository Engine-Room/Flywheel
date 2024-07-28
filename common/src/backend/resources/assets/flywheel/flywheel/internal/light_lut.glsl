const uint _FLW_LIGHT_SECTION_SIZE_BYTES = 18 * 18 * 18;
const uint _FLW_LIGHT_SECTION_SIZE_INTS = _FLW_LIGHT_SECTION_SIZE_BYTES / 4;

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

vec2 _flw_lightAt(uint sectionOffset, uvec3 blockInSectionPos) {
    uint byteOffset = blockInSectionPos.x + blockInSectionPos.z * 18u + blockInSectionPos.y * 18u * 18u;

    uint uintOffset = byteOffset >> 2u;
    uint bitOffset = (byteOffset & 3u) << 3;

    uint raw = _flw_indexLight(sectionOffset + uintOffset);
    uint block = (raw >> bitOffset) & 0xFu;
    uint sky = (raw >> (bitOffset + 4u)) & 0xFu;

    return vec2(block, sky);
}

bool flw_lightFetch(ivec3 blockPos, out vec2 lightCoord) {
    uint lightSectionIndex;
    if (_flw_chunkCoordToSectionIndex(blockPos >> 4, lightSectionIndex)) {
        return false;
    }
    // The offset of the section in the light buffer.
    uint sectionOffset = lightSectionIndex * _FLW_LIGHT_SECTION_SIZE_INTS;

    uvec3 blockInSectionPos = (blockPos & 0xF) + 1;

    lightCoord = _flw_lightAt(sectionOffset, blockInSectionPos) / 15.;
    return true;
}

bool flw_light(vec3 worldPos, out vec2 lightCoord) {
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
    uvec3 blockInSectionPos = (blockPos & 0xF) + 1;

    // The lowest corner of the 2x2x2 area we'll be trilinear interpolating.
    // The ugly bit on the end evaluates to -1 or 0 depending on which side of 0.5 we are.
    uvec3 lowestCorner = blockInSectionPos + ivec3(floor(fract(worldPos) - 0.5));

    // The distance our fragment is from the center of the lowest corner.
    vec3 interpolant = fract(worldPos - 0.5);

    // Fetch everything for trilinear interpolation
    // Hypothetically we could re-order these and do some calculations in-between fetches
    // to help with latency hiding, but the compiler should be able to do that for us.
    vec2 light000 = _flw_lightAt(sectionOffset, lowestCorner);
    vec2 light001 = _flw_lightAt(sectionOffset, lowestCorner + uvec3(0, 0, 1));
    vec2 light010 = _flw_lightAt(sectionOffset, lowestCorner + uvec3(0, 1, 0));
    vec2 light011 = _flw_lightAt(sectionOffset, lowestCorner + uvec3(0, 1, 1));
    vec2 light100 = _flw_lightAt(sectionOffset, lowestCorner + uvec3(1, 0, 0));
    vec2 light101 = _flw_lightAt(sectionOffset, lowestCorner + uvec3(1, 0, 1));
    vec2 light110 = _flw_lightAt(sectionOffset, lowestCorner + uvec3(1, 1, 0));
    vec2 light111 = _flw_lightAt(sectionOffset, lowestCorner + uvec3(1, 1, 1));

    vec2 light00 = mix(light000, light001, interpolant.z);
    vec2 light01 = mix(light010, light011, interpolant.z);
    vec2 light10 = mix(light100, light101, interpolant.z);
    vec2 light11 = mix(light110, light111, interpolant.z);

    vec2 light0 = mix(light00, light01, interpolant.y);
    vec2 light1 = mix(light10, light11, interpolant.y);

    lightCoord = mix(light0, light1, interpolant.x) / 15.;
    return true;
}

uint _flw_lightIndex(in uvec3 p) {
    return p.x + p.z * 3u + p.y * 9u;
}

/// Premtively collect all light in a 3x3x3 area centered on our block.
/// Depending on the normal, we won't use all the data, but fetching on demand will have many duplicated fetches.
vec2[27] _flw_lightFetch3x3x3(uint sectionOffset, ivec3 blockInSectionPos) {
    vec2[27] lights;

    for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++) {
                lights[_flw_lightIndex(uvec3(x + 1, y + 1, z + 1))] = _flw_lightAt(sectionOffset, uvec3(blockInSectionPos + ivec3(x, y, z)));
            }
        }
    }

    return lights;
}

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
vec2 _flw_lightForDirection(in vec2[27] lights, in vec3 interpolant, in uvec3 c00, in uvec3 c01, in uvec3 c10, in uvec3 c11) {

    vec2 light000 = lights[_flw_lightIndex(c00 + uvec3(0u, 0u, 0u))] + lights[_flw_lightIndex(c01 + uvec3(0u, 0u, 0u))] + lights[_flw_lightIndex(c10 + uvec3(0u, 0u, 0u))] + lights[_flw_lightIndex(c11 + uvec3(0u, 0u, 0u))];
    vec2 light001 = lights[_flw_lightIndex(c00 + uvec3(0u, 0u, 1u))] + lights[_flw_lightIndex(c01 + uvec3(0u, 0u, 1u))] + lights[_flw_lightIndex(c10 + uvec3(0u, 0u, 1u))] + lights[_flw_lightIndex(c11 + uvec3(0u, 0u, 1u))];
    vec2 light010 = lights[_flw_lightIndex(c00 + uvec3(0u, 1u, 0u))] + lights[_flw_lightIndex(c01 + uvec3(0u, 1u, 0u))] + lights[_flw_lightIndex(c10 + uvec3(0u, 1u, 0u))] + lights[_flw_lightIndex(c11 + uvec3(0u, 1u, 0u))];
    vec2 light011 = lights[_flw_lightIndex(c00 + uvec3(0u, 1u, 1u))] + lights[_flw_lightIndex(c01 + uvec3(0u, 1u, 1u))] + lights[_flw_lightIndex(c10 + uvec3(0u, 1u, 1u))] + lights[_flw_lightIndex(c11 + uvec3(0u, 1u, 1u))];
    vec2 light100 = lights[_flw_lightIndex(c00 + uvec3(1u, 0u, 0u))] + lights[_flw_lightIndex(c01 + uvec3(1u, 0u, 0u))] + lights[_flw_lightIndex(c10 + uvec3(1u, 0u, 0u))] + lights[_flw_lightIndex(c11 + uvec3(1u, 0u, 0u))];
    vec2 light101 = lights[_flw_lightIndex(c00 + uvec3(1u, 0u, 1u))] + lights[_flw_lightIndex(c01 + uvec3(1u, 0u, 1u))] + lights[_flw_lightIndex(c10 + uvec3(1u, 0u, 1u))] + lights[_flw_lightIndex(c11 + uvec3(1u, 0u, 1u))];
    vec2 light110 = lights[_flw_lightIndex(c00 + uvec3(1u, 1u, 0u))] + lights[_flw_lightIndex(c01 + uvec3(1u, 1u, 0u))] + lights[_flw_lightIndex(c10 + uvec3(1u, 1u, 0u))] + lights[_flw_lightIndex(c11 + uvec3(1u, 1u, 0u))];
    vec2 light111 = lights[_flw_lightIndex(c00 + uvec3(1u, 1u, 1u))] + lights[_flw_lightIndex(c01 + uvec3(1u, 1u, 1u))] + lights[_flw_lightIndex(c10 + uvec3(1u, 1u, 1u))] + lights[_flw_lightIndex(c11 + uvec3(1u, 1u, 1u))];

    vec2 light00 = mix(light000, light001, interpolant.z);
    vec2 light01 = mix(light010, light011, interpolant.z);
    vec2 light10 = mix(light100, light101, interpolant.z);
    vec2 light11 = mix(light110, light111, interpolant.z);

    vec2 light0 = mix(light00, light01, interpolant.y);
    vec2 light1 = mix(light10, light11, interpolant.y);

    // Divide by 60 (15 * 4) to normalize.
    return mix(light0, light1, interpolant.x) / 63.;
}

bool flw_light(vec3 worldPos, vec3 normal, out vec2 lightCoord) {
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

    // Fetch everything in a 3x3x3 area centered around the block.
    vec2[27] lights = _flw_lightFetch3x3x3(sectionOffset, blockInSectionPos);

    vec3 interpolant = fract(worldPos);

    vec2 lightX;
    if (normal.x > 0) {
        lightX = _flw_lightForDirection(lights, interpolant, uvec3(1u, 0u, 0u), uvec3(1u, 0u, 1u), uvec3(1u, 1u, 0u), uvec3(1u, 1u, 1u));
    } else if (normal.x < 0) {
        lightX = _flw_lightForDirection(lights, interpolant, uvec3(0u, 0u, 0u), uvec3(0u, 0u, 1u), uvec3(0u, 1u, 0u), uvec3(0u, 1u, 1u));
    } else {
        lightX = vec2(0.);
    }

    vec2 lightZ;
    if (normal.z > 0) {
        lightZ = _flw_lightForDirection(lights, interpolant, uvec3(0u, 0u, 1u), uvec3(0u, 1u, 1u), uvec3(1u, 0u, 1u), uvec3(1u, 1u, 1u));
    } else if (normal.z < 0) {
        lightZ = _flw_lightForDirection(lights, interpolant, uvec3(0u, 0u, 0u), uvec3(0u, 1u, 0u), uvec3(1u, 0u, 0u), uvec3(1u, 1u, 0u));
    } else {
        lightZ = vec2(0.);
    }

    vec2 lightY;
    // Average the light in relevant directions at each corner.
    if (normal.y > 0.) {
        lightY = _flw_lightForDirection(lights, interpolant, uvec3(0u, 1u, 0u), uvec3(0u, 1u, 1u), uvec3(1u, 1u, 0u), uvec3(1u, 1u, 1u));
    } else if (normal.y < 0.) {
        lightY = _flw_lightForDirection(lights, interpolant, uvec3(0u, 0u, 0u), uvec3(0u, 0u, 1u), uvec3(1u, 0u, 0u), uvec3(1u, 0u, 1u));
    } else {
        lightY = vec2(0.);
    }

    vec3 n2 = normal * normal;
    lightCoord = lightX * n2.x + lightY * n2.y + lightZ * n2.z;

    return true;
}

