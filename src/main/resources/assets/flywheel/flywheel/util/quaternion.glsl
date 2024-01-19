vec4 quaternion(vec3 axis, float angle) {
    float halfAngle = angle * 0.5;
    return vec4(axis.xyz * sin(halfAngle), cos(halfAngle));
}

vec4 quaternionDegrees(vec3 axis, float angle) {
    return quaternion(axis, radians(angle));
}

vec4 multiplyQuaternions(vec4 q1, vec4 q2) {
    // disgustingly vectorized quaternion multiplication
    vec4 a = q1.w * q2.xyzw;
    vec4 b = q1.x * q2.wzxy * vec4(1., -1., 1., -1.);
    vec4 c = q1.y * q2.zwxy * vec4(1., 1., -1., -1.);
    vec4 d = q1.z * q2.yxwz * vec4(-1., 1., 1., -1.);

    return a + b + c + d;
}

vec3 rotateByQuaternion(vec3 v, vec4 q) {
    vec3 i = q.xyz;
    return v + 2.0 * cross(i, cross(i, v) + q.w * v);
}

vec3 rotateAxisAngle(vec3 v, vec3 axis, float angle) {
    return rotateByQuaternion(v, quaternion(axis, angle));
}

vec3 rotateAxisAngleDegrees(vec3 v, vec3 axis, float angle) {
    return rotateByQuaternion(v, quaternionDegrees(axis, angle));
}
