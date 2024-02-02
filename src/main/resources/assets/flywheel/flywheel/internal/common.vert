#include "flywheel:internal/fog_distance.glsl"

void _flw_main(in FlwInstance instance) {
    _flw_layoutVertex();
    flw_beginVertex();
    flw_instanceVertex(instance);
    flw_materialVertex();
    flw_endVertex();

    flw_vertexNormal = normalize(flw_vertexNormal);

    flw_distance = fogDistance(flw_vertexPos.xyz, flw_cameraPos, flw_fogShape);

    gl_Position = flw_viewProjection * flw_vertexPos;
}
