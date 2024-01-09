void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius) {
    mat4 pose = i.pose;
    center = (pose * vec4(center, 1.0)).xyz;

    vec3 c0 = pose[0].xyz;
    vec3 c1 = pose[1].xyz;
    vec3 c2 = pose[2].xyz;

    // Comute the squared maximum to avoid 2 unnecessary sqrts.
    // I don't think this makes it any faster but why not /shrug
    float scaleSqr = max(dot(c0, c0), max(dot(c1, c1), dot(c2, c2)));
    float scale = sqrt(scaleSqr);
    radius *= scale;
}
