
struct Instance {
    vec4 rotation;
    vec3 pos;
    vec3 pivot;
    uint light;
    uint color;
    uint batchID;
};

struct MeshDrawCommand {
    uint indexCount;
    uint instanceCount;
    uint firstIndex;
    uint vertexOffset;
    uint baseInstance;
};
