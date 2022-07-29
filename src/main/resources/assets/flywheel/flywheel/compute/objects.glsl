
struct Instance {
    ivec2 light;
    vec4 color;
    vec3 pos;
    vec3 pivot;
    vec4 rotation;
    uint batchID;
};

struct MeshDrawCommands {
    uint indexCount;
    uint instanceCount;
    uint firstIndex;
    uint vertexOffset;
    uint baseInstance;
};
