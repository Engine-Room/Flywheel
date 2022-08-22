#version 450
#define FLW_SUBGROUP_SIZE 32

layout(local_size_x = FLW_SUBGROUP_SIZE) in;


// in uvec3 gl_NumWorkGroups;
// in uvec3 gl_WorkGroupID;
// in uvec3 gl_LocalInvocationID;
// in uvec3 gl_GlobalInvocationID;
// in uint  gl_LocalInvocationIndex;

layout(std430, binding = 0) buffer Frustum1 {
    vec4 a1; // vec4(nx.x, px.x, ny.x, py.x)
    vec4 a2; // vec4(nx.y, px.y, ny.y, py.y)
    vec4 a3; // vec4(nx.z, px.z, ny.z, py.z)
    vec4 a4; // vec4(nx.w, px.w, ny.w, py.w)
    vec2 b1; // vec2(nz.x, pz.x)
    vec2 b2; // vec2(nz.y, pz.y)
    vec2 b3; // vec2(nz.z, pz.z)
    vec2 b4; // vec2(nz.w, pz.w)
} frustum1;

layout(binding = 1) buffer Frustum2 {
    vec4 nx;
    vec4 px;
    vec4 ny;
    vec4 py;
    vec4 nz;
    vec4 pz;
} frustum2;

layout(binding = 2) buffer Result {
    bool res1;
    bool res2;
    bool res3;
} result;

// 83 - 27 = 56 spirv instruction results
bool testSphere1(vec4 sphere) {
    return
    all(lessThanEqual(fma(frustum1.a1, sphere.xxxx, fma(frustum1.a2, sphere.yyyy, fma(frustum1.a3, sphere.zzzz, frustum1.a4))), -sphere.wwww)) &&
    all(lessThanEqual(fma(frustum1.b1, sphere.xx, fma(frustum1.b2, sphere.yy, fma(frustum1.b3, sphere.zz, frustum1.b4))), -sphere.ww));
}

// 236 - 92 = 144 spirv instruction results
bool testSphere2(vec4 sphere) {
    return
    fma(frustum2.nx.x, sphere.x, fma(frustum2.nx.y, sphere.y, fma(frustum2.nx.z, sphere.z, frustum2.nx.w))) >= -sphere.w &&
    fma(frustum2.px.x, sphere.x, fma(frustum2.px.y, sphere.y, fma(frustum2.px.z, sphere.z, frustum2.px.w))) >= -sphere.w &&
    fma(frustum2.ny.x, sphere.x, fma(frustum2.ny.y, sphere.y, fma(frustum2.ny.z, sphere.z, frustum2.ny.w))) >= -sphere.w &&
    fma(frustum2.py.x, sphere.x, fma(frustum2.py.y, sphere.y, fma(frustum2.py.z, sphere.z, frustum2.py.w))) >= -sphere.w &&
    fma(frustum2.nz.x, sphere.x, fma(frustum2.nz.y, sphere.y, fma(frustum2.nz.z, sphere.z, frustum2.nz.w))) >= -sphere.w &&
    fma(frustum2.pz.x, sphere.x, fma(frustum2.pz.y, sphere.y, fma(frustum2.pz.z, sphere.z, frustum2.pz.w))) >= -sphere.w;
}

// 322 - 240 = 82 spirv instruction results
bool testSphere3(vec4 sphere) {
    return
    (dot(frustum2.nx.xyz, sphere.xyz) + frustum2.nx.w) >= -sphere.w &&
    (dot(frustum2.px.xyz, sphere.xyz) + frustum2.px.w) >= -sphere.w &&
    (dot(frustum2.ny.xyz, sphere.xyz) + frustum2.ny.w) >= -sphere.w &&
    (dot(frustum2.py.xyz, sphere.xyz) + frustum2.py.w) >= -sphere.w &&
    (dot(frustum2.nz.xyz, sphere.xyz) + frustum2.nz.w) >= -sphere.w &&
    (dot(frustum2.pz.xyz, sphere.xyz) + frustum2.pz.w) >= -sphere.w;
}

void main() {
    result.res1 = testSphere1(vec4(0., 1., 0., 1.));
    result.res2 = testSphere2(vec4(0., 1., 0., 1.));
    result.res3 = testSphere3(vec4(0., 1., 0., 1.));
}
