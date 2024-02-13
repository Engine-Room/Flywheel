uniform sampler2D crumblingTex;

in vec2 crumblingTexCoord;

vec4 crumblingSampleColor;

void flw_beginFragment() {
}

void flw_endFragment() {
    crumblingSampleColor = texture(crumblingTex, crumblingTexCoord);

    // Make the crumbling overlay transparent when the fragment color after the material shader is transparent.
    flw_fragColor.rgb = crumblingSampleColor.rgb;
    flw_fragColor.a *= crumblingSampleColor.a;
}
