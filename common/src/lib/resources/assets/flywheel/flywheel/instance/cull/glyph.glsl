#include "flywheel:util/matrix.glsl"

void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius) {

    radius += abs(i.x[2] - i.x[0]) + abs(i.y[1] - i.y[0]);
    center += vec3((i.x[0] + i.x[2]) * 0.5, (i.y[0] + i.y[1]) * 0.5, 0.);

    transformBoundingSphere(i.pose, center, radius);
}
