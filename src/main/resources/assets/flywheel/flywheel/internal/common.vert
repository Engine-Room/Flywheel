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

void _flw_main(in FlwInstance instance, in uint stableInstanceID) {
    _flw_layoutVertex();
    flw_beginVertex();
    flw_instanceVertex(instance);
    flw_materialVertex();
    flw_endVertex();

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
    }
}
