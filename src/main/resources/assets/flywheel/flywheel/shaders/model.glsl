#use "flywheel:core/diffuse.glsl"

struct Instance {
    vec2 light;
    vec4 color;
    mat4 transform;
    mat3 normalMat;
};

struct Vertex {
    vec3 pos;
    vec3 normal;
    vec2 texCoords;
};

struct BlockFrag {
    vec2 texCoords;
    vec4 color;
    float diffuse;
    vec2 light;
};

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

void fragment(BlockFrag r) {
    vec4 tex = FLWBlockTexture(r.texCoords);

    vec4 color = vec4(tex.rgb * FLWLight(r.light).rgb * r.diffuse, tex.a) * r.color;

    //    flw_WorldPos = ;
    //    flw_Normal = ;
    //    flw_Albedo = tex.rgb;
    //    flw_Alpha = tex.a;
    //    flw_LightMap = r.light;
    //    flw_Tint = r.color;
    FLWFinalizeColor(color);
}

