
struct BlockFrag {
    vec2 texCoords;
    vec4 color;
    float diffuse;
    vec2 light;
};

#if defined(FRAGMENT_SHADER)
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
#endif
