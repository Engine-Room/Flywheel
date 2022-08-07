#use "flywheel:api/vertex.glsl"
#use "flywheel:layout/block.vert"
#use "flywheel:context/world.vert"
#use "flywheel:util/quaternion.glsl"
#use "flywheel:instance/oriented_indirect.glsl"

// populated by instancers
layout(std430, binding = 0) restrict readonly buffer ObjectBuffer {
    FLW_INSTANCE_STRUCT objects[];
};

layout(std430, binding = 1) restrict readonly buffer TargetBuffer {
    uint objectIDs[];
};

void main() {
    uint instanceIndex = objectIDs[gl_BaseInstance + gl_InstanceID];
    flw_layoutVertex();
    Instance i = objects[instanceIndex];
    flw_instanceVertex(i);
    flw_contextVertex();
}
