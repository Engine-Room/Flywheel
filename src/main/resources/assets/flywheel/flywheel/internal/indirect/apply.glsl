#include "flywheel:internal/indirect/buffers.glsl"
#include "flywheel:internal/indirect/model_descriptor.glsl"
#include "flywheel:internal/indirect/draw_command.glsl"

layout(local_size_x = _FLW_SUBGROUP_SIZE) in;

layout(std430, binding = _FLW_MODEL_BUFFER_BINDING) restrict readonly buffer ModelBuffer {
    ModelDescriptor models[];
};

layout(std430, binding = _FLW_DRAW_BUFFER_BINDING) restrict buffer DrawBuffer {
    MeshDrawCommand drawCommands[];
};

// Apply the results of culling to the draw commands.
void main() {
    uint drawIndex = gl_GlobalInvocationID.x;

    if (drawIndex >= drawCommands.length()) {
        return;
    }

    uint modelIndex = drawCommands[drawIndex].modelIndex;
    uint instanceCount = models[modelIndex].instanceCount;
    drawCommands[drawIndex].instanceCount = instanceCount;
}
