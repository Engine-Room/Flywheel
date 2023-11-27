#include "flywheel:api/vertex.glsl"
#include "flywheel:util/fog.glsl"

out vec2 _flw_crumblingFlip;

const int DOWN = 0;
const int UP = 1;
const int NORTH = 2;
const int SOUTH = 3;
const int WEST = 4;
const int EAST = 5;

const vec2 FLIPS_BY_FACE[6] = vec2[](
    vec2(1., -1.),
    vec2(-1., -1.),
    vec2(-1., -1.),
    vec2(-1., -1.),
    vec2(1., -1.),
    vec2(1., -1.)
);

// based on net.minecraftforge.client.ForgeHooksClient.getNearestStable
int getNearestFacing(vec3 normal) {
    float maxAlignment = -2;
    int face = 2;

    vec3 alignment = vec3(
    dot(normal, vec3(1., 0., 0.)),
    dot(normal, vec3(0., 1., 0.)),
    dot(normal, vec3(0., 0., 1.))
    );

    if (-alignment.y > maxAlignment) {
        maxAlignment = -alignment.y;
        face = DOWN;
    }
    if (alignment.y > maxAlignment) {
        maxAlignment = alignment.y;
        face = UP;
    }
    if (-alignment.z > maxAlignment) {
        maxAlignment = -alignment.z;
        face = NORTH;
    }
    if (alignment.z > maxAlignment) {
        maxAlignment = alignment.z;
        face = SOUTH;
    }
    if (alignment.x > maxAlignment) {
        maxAlignment = -alignment.x;
        face = WEST;
    }

    return face;
}

vec2 calculateFlip(vec3 normal) {
    int face = getNearestFacing(normal);
    return FLIPS_BY_FACE[face];
}

// This is disgusting so if an issue comes up just throw this away and fix the branching version above.
vec2 calculateFlipBranchless(vec3 normal) {
    vec3 alignment = vec3(
    dot(normal, vec3(1., 0., 0.)),
    dot(normal, vec3(0., 1., 0.)),
    dot(normal, vec3(0., 0., 1.))
    );

    vec3 absAlignment = abs(alignment);

    // x is the max alignment that would cause U to be -1.
    // y is the max alignment that would cause U to be 1.
    vec2 maxNegativeMaxPositive = max(vec2(absAlignment.z, alignment.y), vec2(-alignment.y, absAlignment.x));

    bool flipU = maxNegativeMaxPositive.x > maxNegativeMaxPositive.y;

    return vec2(mix(1., -1., flipU), -1.);
}

void flw_initVertex() {
    // Calculate the flips in model space so that the crumbling effect doesn't have discontinuities.
    _flw_crumblingFlip = calculateFlipBranchless(flw_vertexNormal);
}

void flw_contextVertex() {
    flw_distance = fog_distance(flw_vertexPos.xyz, flywheel.cameraPos.xyz, flywheel.fogShape);
    gl_Position = flywheel.viewProjection * flw_vertexPos;
    flw_vertexNormal = normalize(flw_vertexNormal);
}
