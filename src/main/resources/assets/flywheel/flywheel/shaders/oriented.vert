#use "flywheel:core/matutils.glsl"
#use "flywheel:core/quaternion.glsl"
#use "flywheel:core/diffuse.glsl"

#use "flywheel:data/modelvertex.glsl"
#use "flywheel:block.frag"

struct Oriented {
    vec2 light;
    vec4 color;
    vec3 pos;
    vec3 pivot;
    vec4 rotation;
};

#if defined(VERTEX_SHADER)
BlockFrag vertex(Vertex v, Oriented o) {
    vec4 worldPos = vec4(rotateVertexByQuat(v.pos - o.pivot, o.rotation) + o.pivot + o.pos, 1.);

    vec3 norm = rotateVertexByQuat(v.normal, o.rotation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = o.light;
    #if defined(DEBUG_NORMAL)
    b.color = vec4(norm, 1.);
    #else
    b.color = o.color;
    #endif
    return b;
}
#endif
