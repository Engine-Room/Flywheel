// Types intended for use is SSBOs to achieve tighter data packing.

struct Vec3F {
    float x;
    float y;
    float z;
};

struct Vec4F {
    float x;
    float y;
    float z;
    float w;
};

struct Mat4F {
    Vec4F c0;
    Vec4F c1;
    Vec4F c2;
    Vec4F c3;
};

struct Mat3F {
    Vec3F c0;
    Vec3F c1;
    Vec3F c2;
};

// 4-aligned instead of a 16-aligned vec4
struct BoundingSphere {
    Vec3F center;
    float radius;
};

struct LightCoord {
    uint p;
};

vec3 unpackVec3F(in Vec3F v) {
    return vec3(v.x, v.y, v.z);
}

vec4 unpackVec4F(in Vec4F v) {
    return vec4(v.x, v.y, v.z, v.w);
}

mat4 unpackMat4F(in Mat4F m) {
    return mat4(
    unpackVec4F(m.c0),
    unpackVec4F(m.c1),
    unpackVec4F(m.c2),
    unpackVec4F(m.c3)
    );
}

mat3 unpackMat3F(in Mat3F m) {
    return mat3(
    unpackVec3F(m.c0),
    unpackVec3F(m.c1),
    unpackVec3F(m.c2)
    );
}

void unpackBoundingSphere(in BoundingSphere sphere, out vec3 center, out float radius) {
    center = unpackVec3F(sphere.center);
    radius = sphere.radius;
}

vec2 unpackLightCoord(in LightCoord light) {
    return vec2(float((light.p >> 16) & 0xFFFFu), float(light.p & 0xFFFFu)) / 15.0;
}
