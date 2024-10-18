#include "flywheel:internal/common.frag"
#include "flywheel:internal/instancing/light.glsl"

uniform uvec2 _flw_packedMaterial;

flat in uint _flw_instanceID;

void main() {
    _flw_unpackUint2x16(_flw_packedMaterial.x, _flw_uberFogIndex, _flw_uberCutoutIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.y, flw_material);

    _flw_main(_flw_instanceID);
}
