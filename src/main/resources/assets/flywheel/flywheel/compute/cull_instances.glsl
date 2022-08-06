#define FLW_SUBGROUP_SIZE 32
layout(local_size_x = FLW_SUBGROUP_SIZE) in;
#use "flywheel:compute/objects.glsl"
#use "flywheel:util/quaternion.glsl"

uint flw_objectID;
uint flw_batchID;

layout(std140, binding = 0) uniform FrameData {
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

layout(std430, binding = 4) writeonly buffer DebugVisibility {
    uint objectVisibilityBits[];
};

// 83 - 27 = 56 spirv instruction results
bool testSphere(vec3 center, float radius) {
    bvec4 resultA = greaterThanEqual(fma(frustum.a1, center.xxxx, fma(frustum.a2, center.yyyy, fma(frustum.a3, center.zzzz, frustum.a4))), -radius.xxxx);
    bvec2 resultB = greaterThanEqual(fma(frustum.b1, center.xx, fma(frustum.b2, center.yy, fma(frustum.b3, center.zz, frustum.b4))), -radius.xx);

    uint debug = uint(resultA.x);
    debug |= uint(resultA.y) << 1;
    debug |= uint(resultA.z) << 2;
    debug |= uint(resultA.w) << 3;
    debug |= uint(resultB.x) << 4;
    debug |= uint(resultB.y) << 5;

    objectVisibilityBits[flw_objectID] = debug;

    return all(resultA) && all(resultB);
}

void flw_transformBoundingSphere(in Instance i, inout vec3 center, inout float radius) {
    center = rotateVertexByQuat(center - i.pivot, i.rotation) + i.pivot + i.pos;
    radius = radius;
}

bool isVisible() {
    vec4 sphere = boundingSpheres[flw_batchID];

    vec3 center = sphere.xyz;
    float radius = sphere.r;
    flw_transformBoundingSphere(objects[flw_objectID], center, radius);

    return testSphere(center, radius);
}

void main() {
    flw_objectID = gl_GlobalInvocationID.x;

    if (flw_objectID >= objects.length()) {
        return;
    }

    flw_batchID = objects[objectID].batchID;

    if (isVisible()) {
        uint batchIndex = atomicAdd(drawCommands[flw_batchID].instanceCount, 1);
        uint globalIndex = drawCommands[flw_batchID].baseInstance + batchIndex;

        objectIDs[globalIndex] = flw_objectID;
    }
}
