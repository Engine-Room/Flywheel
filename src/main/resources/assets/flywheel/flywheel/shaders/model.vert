#use "flywheel:core/diffuse.glsl"
#use "flywheel:data/modelvertex.glsl"
#use "flywheel:block.frag"

struct Instance {
    vec2 light;
    vec4 color;
    mat4 transform;
    mat3 normalMat;
};

#if defined(VERTEX_SHADER)
BlockFrag vertex(Vertex v, Instance i) {
    vec4 worldPos = i.transform * vec4(v.pos, 1.);

    vec3 norm = i.normalMat * v.normal;

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    norm = normalize(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = i.light;
    #if defined(DEBUG_NORMAL)
    b.color = vec4(norm, 1.);
    #else
    b.color = i.color;
    #endif
    return b;
}
#endif
