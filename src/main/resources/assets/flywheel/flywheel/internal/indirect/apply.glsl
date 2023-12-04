#include "flywheel:internal/indirect/buffers.glsl"
#include "flywheel:internal/indirect/model_descriptor.glsl"
#include "flywheel:internal/indirect/draw_command.glsl"

layout(local_size_x = FLW_SUBGROUP_SIZE) in;

layout(std430, binding = MODEL_BINDING) restrict readonly buffer ModelDescriptors {
    ModelDescriptor models[];
};

layout(std430, binding = DRAW_BINDING) restrict buffer MeshDrawCommands {
    MeshDrawCommand drawCommands[];
};

// Apply the results of culling to the draw commands.
void main() {
    uint drawID = gl_GlobalInvocationID.x;

    if (drawID >= drawCommands.length()) {
        return;
    }

    uint modelID = drawCommands[drawID].modelID;

    uint instanceCount = models[modelID].instanceCount;

    drawCommands[drawID].instanceCount = instanceCount;
}
