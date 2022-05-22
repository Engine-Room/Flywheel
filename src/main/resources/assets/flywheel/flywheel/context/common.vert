#use "flywheel:api/vertex.glsl"
#use "flywheel:util/fog.glsl"

uniform mat4 uViewProjection;
uniform vec3 uCameraPos;
uniform int uFogShape;

void flw_contextVertex() {
    // TODO: remove this
    #ifdef DEBUG_NORMAL
    flw_vertexColor = vec4(flw_vertexNormal, 1.0);
    #endif

    flw_distance = fog_distance(flw_vertexPos.xyz, uCameraPos, uFogShape);
    gl_Position = uViewProjection * flw_vertexPos;
    flw_vertexNormal = normalize(flw_vertexNormal);
}
