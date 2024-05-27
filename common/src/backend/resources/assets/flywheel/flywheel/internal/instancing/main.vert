#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"

uniform uvec4 _flw_packedMaterial;
uniform int _flw_baseInstance = 0;

#ifdef _FLW_EMBEDDED
bool _flw_embeddedLight(vec3 worldPos, vec3 normal, out vec2 lightCoord) {
    return true;
}
#endif

void main() {
    _flw_uberMaterialVertexIndex = _flw_packedMaterial.x;
    _flw_unpackMaterialProperties(_flw_packedMaterial.w, flw_material);

    FlwInstance instance = _flw_unpackInstance(_flw_baseInstance + gl_InstanceID);

    _flw_main(instance, uint(gl_InstanceID));
}
