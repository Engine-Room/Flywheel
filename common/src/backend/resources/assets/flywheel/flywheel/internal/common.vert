#include "flywheel:internal/fog_distance.glsl"

#ifdef _FLW_CRUMBLING
out vec2 _flw_crumblingTexCoord;

const int DOWN = 0;
const int UP = 1;
const int NORTH = 2;
const int SOUTH = 3;
const int WEST = 4;
const int EAST = 5;

// based on net.minecraftforge.client.ForgeHooksClient.getNearestStable
int getNearestFacing(vec3 normal) {
    float maxAlignment = -2;
    int face = 2;

    // Calculate the alignment of the normal vector with each axis.
    // Note that `-dot(normal, axis) == dot(normal, -axis)`.
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
    if (-alignment.x > maxAlignment) {
        maxAlignment = -alignment.x;
        face = WEST;
    }
    if (alignment.x > maxAlignment) {
        maxAlignment = alignment.x;
        face = EAST;
    }

    return face;
}

vec2 getCrumblingTexCoord() {
    switch (getNearestFacing(flw_vertexNormal)) {
        case DOWN: return vec2(flw_vertexPos.x, -flw_vertexPos.z);
        case UP: return vec2(flw_vertexPos.x, flw_vertexPos.z);
        case NORTH: return vec2(-flw_vertexPos.x, -flw_vertexPos.y);
        case SOUTH: return vec2(flw_vertexPos.x, -flw_vertexPos.y);
        case WEST: return vec2(-flw_vertexPos.z, -flw_vertexPos.y);
        case EAST: return vec2(flw_vertexPos.z, -flw_vertexPos.y);
    }

    // default to north
    return vec2(-flw_vertexPos.x, -flw_vertexPos.y);
}
#endif

#ifdef FLW_EMBEDDED
mat4 _flw_modelMatrix;
mat3 _flw_normalMatrix;
#endif

void _flw_main(in FlwInstance instance) {
    _flw_layoutVertex();
    flw_instanceVertex(instance);
    flw_materialVertex();

    #ifdef _FLW_CRUMBLING
    _flw_crumblingTexCoord = getCrumblingTexCoord();
    #endif

    #ifdef FLW_EMBEDDED
    flw_vertexPos = _flw_modelMatrix * flw_vertexPos;
    flw_vertexNormal = _flw_normalMatrix * flw_vertexNormal;
    #endif

    flw_vertexNormal = normalize(flw_vertexNormal);

    flw_distance = fogDistance(flw_vertexPos.xyz, flw_cameraPos, flw_fogShape);

    gl_Position = flw_viewProjection * flw_vertexPos;
}
