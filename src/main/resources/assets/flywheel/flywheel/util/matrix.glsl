mat3 rotation(vec3 axis, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1. - c;

    vec3 sa = axis * s;

    return mat3(
    oc * axis.xxz * axis.xyx + vec3(c, sa.z, -sa.y),
    oc * axis.xyy * axis.yyz + vec3(-sa.z, c, sa.x),
    oc * axis.zyz * axis.xzz + vec3(sa.y, -sa.x, c)
    );
}

mat3 rotationDegrees(vec3 axis, float angle) {
    return rotation(axis, radians(angle));
}

/*
 * Create a rotation matrix that rotates the vector `from` to `to`.
 * https://iquilezles.org/articles/noacos/
 */
mat3 rotationAlign(in vec3 from, in vec3 to) {
    vec3 v = cross(from, to);
    float c = dot(from, to);
    float k = 1. / (1. + c);

    return mat3(
    k * v * v.xxx + vec3(c, -v.z, v.y),
    k * v * v.yyy + vec3(v.z, c, -v.x),
    k * v * v.zzz + vec3(-v.y, v.x, c)
    );
}

mat3 modelToNormal(mat4 mat) {
    // Discard the edges. This won't be accurate for scaled or skewed matrices,
    // but we don't have to work with those often.
    mat3 m;
    m[0] = mat[0].xyz;
    m[1] = mat[1].xyz;
    m[2] = mat[2].xyz;
    return m;
}
