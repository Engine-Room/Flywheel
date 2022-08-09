#use "flywheel:api/vertex.glsl"

layout(std430, binding = 0) restrict readonly buffer ObjectBuffer {
    FLW_INSTANCE_STRUCT objects[];
};

layout(std430, binding = 1) restrict readonly buffer TargetBuffer {
    uint objectIDs[];
};

void main() {
    uint instanceIndex = objectIDs[gl_BaseInstance + gl_InstanceID];
    flw_layoutVertex();
    FLW_INSTANCE_STRUCT i = objects[instanceIndex];
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_contextVertex();
}
