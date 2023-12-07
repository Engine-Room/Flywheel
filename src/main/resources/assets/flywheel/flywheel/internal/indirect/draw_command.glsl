struct MeshDrawCommand {
    uint indexCount;
    uint instanceCount;
    uint firstIndex;
    uint vertexOffset;
    uint baseInstance;

    uint modelIndex;

    uint materialVertexIndex;
    uint materialFragmentIndex;
    uint packedFogAndCutout;
    uint packedMaterialProperties;
};
