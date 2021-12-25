
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

    FLWFinalizeColor(color);
}
#endif
