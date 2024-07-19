#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/instancing/light.glsl"

uniform uvec4 _flw_packedMaterial;
uniform int _flw_baseInstance = 0;

void main() {
    _flw_uberMaterialVertexIndex = _flw_packedMaterial.x;
    _flw_unpackMaterialProperties(_flw_packedMaterial.w, flw_material);

    FlwInstance instance = _flw_unpackInstance(_flw_baseInstance + gl_InstanceID);

    _flw_main(instance, uint(gl_InstanceID));
}
