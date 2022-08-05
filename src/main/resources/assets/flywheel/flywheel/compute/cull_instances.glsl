#define FLW_SUBGROUP_SIZE 32
layout(local_size_x = FLW_SUBGROUP_SIZE) in;
#use "flywheel:compute/objects.glsl"
#use "flywheel:util/quaternion.glsl"

layout(std140, binding = 3) uniform FrameData {
    vec4 a1; // vec4(nx.x, px.x, ny.x, py.x)
    vec4 a2; // vec4(nx.y, px.y, ny.y, py.y)
    vec4 a3; // vec4(nx.z, px.z, ny.z, py.z)
    vec4 a4; // vec4(nx.w, px.w, ny.w, py.w)
    vec2 b1; // vec2(nz.x, pz.x)
    vec2 b2; // vec2(nz.y, pz.y)
    vec2 b3; // vec2(nz.z, pz.z)
    vec2 b4; // vec2(nz.w, pz.w)
} frustum;

// populated by instancers
layout(std430, binding = 0) readonly buffer ObjectBuffer {
    Instance objects[];
};

layout(std430, binding = 1) writeonly buffer TargetBuffer {
    uint objectIDs[];
};

layout(std430, binding = 2) readonly buffer BoundingSpheres {
    vec4 boundingSpheres[];
};

layout(std430, binding = 3) buffer DrawCommands {
    MeshDrawCommand drawCommands[];
};

// 83 - 27 = 56 spirv instruction results
bool testSphere(vec3 center, float radius) {
    return
    all(lessThanEqual(fma(frustum.a1, center.xxxx, fma(frustum.a2, center.yyyy, fma(frustum.a3, center.zzzz, frustum.a4))), -radius.xxxx)) &&
    all(lessThanEqual(fma(frustum.b1, center.xx, fma(frustum.b2, center.yy, fma(frustum.b3, center.zz, frustum.b4))), -radius.xx));
}

bool isVisible(uint objectID, uint batchID) {
    vec4 sphere = boundingSpheres[batchID];

    vec3 pivot = objects[objectID].pivot;
    vec3 center = rotateVertexByQuat(sphere.xyz - pivot, objects[objectID].rotation) + pivot + objects[objectID].pos;
    float radius = sphere.r;

    return true; //testSphere(center, radius);
}

void main() {
    uint objectID = gl_GlobalInvocationID.x;

    if (objectID >= objects.length()) {
        return;
    }

    uint batchID = objects[objectID].batchID;
    bool visible = isVisible(objectID, batchID);

    if (visible) {
        uint batchIndex = atomicAdd(drawCommands[batchID].instanceCount, 1);
        uint globalIndex = drawCommands[batchID].baseInstance + batchIndex;

        objectIDs[globalIndex] = objectID;
    }
}
