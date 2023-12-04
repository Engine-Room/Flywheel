struct MeshDrawCommand {
    uint indexCount;
    uint instanceCount;
    uint firstIndex;
    uint vertexOffset;
    uint baseInstance;

    uint modelID;
    uint vertexMaterialID;
    uint fragmentMaterialID;
    uint packedFogAndCutout;
    uint packedMaterialProperties;
};
