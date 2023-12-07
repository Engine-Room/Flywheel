struct BoundingSphere {
    float x;
    float y;
    float z;
    float radius;
};

struct ModelDescriptor {
    uint instanceCount;
    uint baseInstance;
    BoundingSphere boundingSphere;
};

void _flw_unpackBoundingSphere(in BoundingSphere sphere, out vec3 center, out float radius) {
    center = vec3(sphere.x, sphere.y, sphere.z);
    radius = sphere.radius;
}
