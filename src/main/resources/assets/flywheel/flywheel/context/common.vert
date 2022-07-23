#use "flywheel:api/vertex.glsl"
#use "flywheel:util/fog.glsl"
#use "flywheel:uniform/fog.glsl"
#use "flywheel:uniform/view.glsl"

void flw_contextVertex() {
    // TODO: remove this
    #ifdef DEBUG_NORMAL
    flw_vertexColor = vec4(flw_vertexNormal, 1.0);
    #endif

    flw_distance = fog_distance(flw_vertexPos.xyz, flw_cameraPos.xyz, flw_fogShape);
    gl_Position = flw_viewProjection * flw_vertexPos;
    flw_vertexNormal = normalize(flw_vertexNormal);
}
