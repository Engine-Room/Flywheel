struct Matrices {
    mat4 pose;
    vec4 normalA;
    vec4 normalB;
    vec4 normalC;
};

void _flw_unpackMatrices(in Matrices mats, out mat4 pose, out mat3 normal) {
    pose = mats.pose;
    normal = mat3(mats.normalA.xyz, mats.normalB.xyz, mats.normalC.xyz);
}
