#use "flywheel:api/vertex.glsl"
#use "flywheel:util/diffuse.glsl"
#use "flywheel:util/fog.glsl"

uniform mat4 uViewProjection;
uniform vec3 uCameraPos;
uniform int uConstantAmbientLight;
uniform int uFogShape;

out float _flw_diffuse;

void flw_contextVertex() {
    flw_vertexNormal = normalize(flw_vertexNormal);
    if (uConstantAmbientLight == 1) {
        _flw_diffuse = diffuseNether(flw_vertexNormal);
    } else {
        _flw_diffuse = diffuse(flw_vertexNormal);
    }
    flw_distance = fog_distance(flw_vertexPos.xyz, uCameraPos, uFogShape);
    gl_Position = uViewProjection * flw_vertexPos;

    // TODO: remove this
    #ifdef DEBUG_NORMAL
    flw_vertexColor = vec4(flw_vertexNormal, 1.0);
    #endif
}
