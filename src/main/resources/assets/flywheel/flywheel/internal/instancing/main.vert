#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"

uniform uvec4 _flw_packedMaterial;

void main() {
    _flw_uberMaterialVertexIndex = _flw_packedMaterial.x;
    _flw_unpackMaterialProperties(_flw_packedMaterial.w, flw_material);

    FlwInstance instance = _flw_unpackInstance();

    _flw_main(instance);
}
