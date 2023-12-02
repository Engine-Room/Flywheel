struct BoundingSphere {
	float x;
	float y;
	float z;
	float radius;
};

void unpackBoundingSphere(in BoundingSphere sphere, out vec3 center, out float radius) {
	center = vec3(sphere.x, sphere.y, sphere.z);
	radius = sphere.radius;
}

struct MeshDrawCommand {
	uint indexCount;
	uint instanceCount;
	uint firstIndex;
	uint vertexOffset;
	uint baseInstance;

	BoundingSphere boundingSphere;
	uint vertexMaterialID;
    uint fragmentMaterialID;
    uint packedFogAndCutout;
    uint packedMaterialProperties;
};
