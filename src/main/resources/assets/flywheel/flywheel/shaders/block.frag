
struct Fragment {
    vec2 texCoords;
    vec4 color;
    float diffuse;
    vec2 light;
};

vec4 fragment(Fragment r) {
    vec4 tex = FLWBlockTexture(r.texCoords);

    return vec4(tex.rgb * FLWLight(r.light).rgb * r.diffuse, tex.a) * r.color;
}
