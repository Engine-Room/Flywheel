
struct Vec3F {
    float x;
    float y;
    float z;
};

// 4-aligned instead of a 16-aligned vec4
struct BoundingSphere {
    Vec3F center;
    float radius;
};

void unpackBoundingSphere(in BoundingSphere sphere, out vec3 center, out float radius) {
    center = vec3(sphere.center.x, sphere.center.y, sphere.center.z);
    radius = sphere.radius;
}
