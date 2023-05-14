#include "flywheel:api/vertex.glsl"
#include "flywheel:util/fog.glsl"

void flw_contextVertex() {
    flw_distance = fog_distance(flw_vertexPos.xyz, flywheel.cameraPos.xyz, flywheel.fogShape);
    gl_Position = flywheel.viewProjection * flw_vertexPos;
    flw_vertexNormal = normalize(flw_vertexNormal);
}
