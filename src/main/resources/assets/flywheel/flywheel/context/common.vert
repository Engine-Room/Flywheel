#use "flywheel:api/vertex.glsl"
#use "flywheel:util/fog.glsl"
#use "flywheel:uniform/fog.glsl"
#use "flywheel:uniform/view.glsl"

void flw_contextVertex() {
    flw_distance = fog_distance(flw_vertexPos.xyz, flw_cameraPos.xyz, flw_fogShape);
    gl_Position = flw_viewProjection * flw_vertexPos;
    flw_vertexNormal = normalize(flw_vertexNormal);
}
