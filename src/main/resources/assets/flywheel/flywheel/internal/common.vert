#include "flywheel:internal/fog_distance.glsl"

// https://stackoverflow.com/a/17479300
uint _flw_hash(in uint x) {
    x += (x << 10u);
    x ^= (x >> 6u);
    x += (x << 3u);
    x ^= (x >> 11u);
    x += (x << 15u);
    return x;
}

vec4 _flw_id2Color(in uint id) {
    uint x = _flw_hash(id);

    return vec4(
        float(x & 0xFFu) / 255.0,
        float((x >> 8u) & 0xFFu) / 255.0,
        float((x >> 16u) & 0xFFu) / 255.0,
        1.
    );
}

out vec4 _flw_debugColor;

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

#ifdef _FLW_EMBEDDED
uniform vec3 _flw_oneOverLightBoxSize;
uniform vec3 _flw_lightVolumeMin;
uniform mat4 _flw_model;
uniform mat3 _flw_normal;

out vec3 _flw_lightVolumeCoord;
#endif


void _flw_main(in FlwInstance instance, in uint stableInstanceID) {
    _flw_layoutVertex();
    flw_instanceVertex(instance);
    flw_materialVertex();

    #ifdef _FLW_CRUMBLING
    _flw_crumblingTexCoord = getCrumblingTexCoord();
    #endif

    #ifdef _FLW_EMBEDDED
    flw_vertexPos = _flw_model * flw_vertexPos;
    flw_vertexNormal = _flw_normal * flw_vertexNormal;

    _flw_lightVolumeCoord = (flw_vertexPos.xyz - _flw_lightVolumeMin) * _flw_oneOverLightBoxSize;
    #endif

    flw_vertexNormal = normalize(flw_vertexNormal);

    flw_distance = fogDistance(flw_vertexPos.xyz, flw_cameraPos, flw_fogShape);

    gl_Position = flw_viewProjection * flw_vertexPos;

    switch (_flw_debugMode) {
    case 0u:
        _flw_debugColor = vec4(1.);
        break;
    case 1u:
        _flw_debugColor = vec4(flw_vertexNormal * .5 + .5, 1.);
        break;
    case 2u:
        _flw_debugColor = _flw_id2Color(stableInstanceID);
        break;
    case 3u:
        _flw_debugColor = vec4(vec2((flw_vertexLight * 15.0 + 0.5) / 16.), 0., 1.);
        break;
    case 4u:
        _flw_debugColor = vec4(flw_vertexOverlay / 16., 0., 1.);
        break;
        #ifdef _FLW_LIGHT_VOLUME
        case 5u:
        _flw_debugColor = vec4(_flw_lightVolumeCoord, 1.);
        break;
        #endif
    }
}
