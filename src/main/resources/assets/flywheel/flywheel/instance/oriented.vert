#include "flywheel:util/quaternion.glsl"

void flw_instanceVertex(in FlwInstance i) {
    flw_vertexPos = vec4(rotateByQuaternion(flw_vertexPos.xyz - i.pivot, i.rotation) + i.pivot + i.position, 1.0);
    flw_vertexNormal = rotateByQuaternion(flw_vertexNormal, i.rotation);
    flw_vertexColor *= i.color;
    flw_vertexOverlay = i.overlay;
    flw_vertexLight = i.light / 15.;
}
