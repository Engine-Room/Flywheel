#use "flywheel:util/types.glsl"

struct MeshDrawCommand {
    uint indexCount;
    uint instanceCount;
    uint firstIndex;
    uint vertexOffset;
    uint baseInstance;

    BoundingSphere boundingSphere;
    uint vertexMaterialID;
    uint fragmentMaterialID;
};
