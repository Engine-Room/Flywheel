#include "flywheel:internal/instancing/api/vertex.glsl"
#include "flywheel:internal/material.glsl"
#include "flywheel:util/diffuse.glsl"

uniform uvec4 _flw_material_instancing;

void main() {
    _flw_materialVertexID = _flw_material_instancing.x;

    _flw_unpackMaterial(_flw_material_instancing.w, flw_material);

    FlwInstance i = _flw_unpackInstance();

    flw_layoutVertex();
    flw_beginVertex();
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_endVertex();

    flw_vertexNormal = normalize(flw_vertexNormal);

    if (flw_material.diffuse) {
        float diffuseFactor;
        if (flywheel.constantAmbientLight == 1) {
            diffuseFactor = diffuseNether(flw_vertexNormal);
        } else {
            diffuseFactor = diffuse(flw_vertexNormal);
        }
        flw_vertexColor = vec4(flw_vertexColor.rgb * diffuseFactor, flw_vertexColor.a);
    }

    flw_distance = fog_distance(flw_vertexPos.xyz, flywheel.cameraPos.xyz, flywheel.fogShape);
    gl_Position = flywheel.viewProjection * flw_vertexPos;
}
