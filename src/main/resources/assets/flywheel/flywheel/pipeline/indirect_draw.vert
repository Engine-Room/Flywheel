#use "flywheel:api/vertex.glsl"

layout(std430, binding = 0) restrict readonly buffer ObjectBuffer {
    FlwPackedInstance objects[];
};

layout(std430, binding = 1) restrict readonly buffer TargetBuffer {
    uint objectIDs[];
};

void main() {
    uint instanceIndex = objectIDs[gl_BaseInstance + gl_InstanceID];
    FlwInstance i = flw_unpackInstance(objects[instanceIndex]);
    flw_layoutVertex();
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_contextVertex();
}
