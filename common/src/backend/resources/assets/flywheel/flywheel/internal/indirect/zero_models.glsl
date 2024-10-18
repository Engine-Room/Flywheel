#include "flywheel:internal/indirect/buffer_bindings.glsl"
#include "flywheel:internal/indirect/model_descriptor.glsl"

layout(local_size_x = _FLW_SUBGROUP_SIZE) in;

layout(std430, binding = _FLW_MODEL_BUFFER_BINDING) restrict writeonly buffer ModelBuffer {
    ModelDescriptor models[];
};

void main() {
    uint modelIndex = gl_GlobalInvocationID.x;

    if (modelIndex >= models.length()) {
        return;
    }

    models[modelIndex].instanceCount = 0;
}
