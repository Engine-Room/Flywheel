#version 110

#flwbeginbody

#FLWPrefixFields(Fragment, varying, v2f_)

//vec3 flw_WorldPos;
//vec3 flw_Normal;
//vec3 flw_Albedo;
//float flw_Alpha;
//vec2 flw_LightMap;
//vec4 flw_Tint;

void main() {
    Fragment f;
    #FLWAssignFields(Fragment, f., v2f_)

    FLWMain(f);
}
