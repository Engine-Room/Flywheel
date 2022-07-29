#use "flywheel:api/vertex.glsl"
#use "flywheel:compute/objects.glsl"
#use "flywheel:pos_tex_normal.glsl"
#use "flywheel:context/world.vert"

// populated by instancers
layout(binding = 0) readonly buffer ObjectBuffer {
    Instance objects[];
};

layout(binding = 1) readonly buffer TargetBuffer {
    uint objectIDs[];
};

void flw_instanceVertex(Instance i) {
    flw_vertexPos = vec4(rotateVertexByQuat(flw_vertexPos.xyz - i.pivot, i.rotation) + i.pivot + i.pos, 1.0);
    flw_vertexNormal = rotateVertexByQuat(flw_vertexNormal, i.rotation);
    flw_vertexColor = i.color;
    flw_vertexLight = i.light / 15.0;
}

void main() {
    uint instanceIndex = objectIDs[gl_BaseInstance + gl_InstanceID];
    flw_layoutVertex();
    Instance i = objects[instanceIndex];
    flw_instanceVertex(i);
    flw_contextVertex();
}
