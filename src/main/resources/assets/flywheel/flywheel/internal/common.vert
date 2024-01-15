#include "flywheel:internal/fog_distance.glsl"

flat out uint _flw_vertexDiffuse;

void _flw_main(in FlwInstance instance) {
    flw_vertexDiffuse = flw_material.diffuse;

    _flw_layoutVertex();
    flw_beginVertex();
    flw_instanceVertex(instance);
    flw_materialVertex();
    flw_endVertex();

    flw_vertexNormal = normalize(flw_vertexNormal);

    _flw_vertexDiffuse = uint(flw_vertexDiffuse);

    flw_distance = fogDistance(flw_vertexPos.xyz, flywheel.cameraPos.xyz, flywheel.fogShape);
    gl_Position = flywheel.viewProjection * flw_vertexPos;
}
