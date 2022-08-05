#use "flywheel:api/vertex.glsl"
#use "flywheel:compute/objects.glsl"
#use "flywheel:layout/block.vert"
#use "flywheel:context/world.vert"
#use "flywheel:util/quaternion.glsl"

// populated by instancers
layout(std430, binding = 0) readonly buffer ObjectBuffer {
    Instance objects[];
};

layout(std430, binding = 1) readonly buffer TargetBuffer {
    uint objectIDs[];
};

void flw_instanceVertex(Instance i) {
    flw_vertexPos = vec4(rotateVertexByQuat(flw_vertexPos.xyz - i.pivot, i.rotation) + i.pivot + i.pos, 1.0);
    flw_vertexNormal = rotateVertexByQuat(flw_vertexNormal, i.rotation);
    flw_vertexColor = unpackUnorm4x8(i.color);
    flw_vertexLight = unpackUnorm2x16(i.light) / 15.0;
}

void main() {
    uint instanceIndex = objectIDs[gl_BaseInstance + gl_InstanceID];
    flw_layoutVertex();
    Instance i = objects[instanceIndex];
    flw_instanceVertex(i);
    flw_contextVertex();
}
