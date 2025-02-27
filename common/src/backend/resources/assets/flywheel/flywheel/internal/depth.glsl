float linearize_depth(float d, float zNear, float zFar) {
    float z_n = 2.0 * d - 1.0;
    return 2.0 * zNear * zFar / (zFar + zNear - z_n * (zFar - zNear));
}

float delinearize_depth(float linearDepth, float zNear, float zFar) {
    float z_n = (2.0 * zNear * zFar / linearDepth) - (zFar + zNear);
    return 0.5 * (z_n / (zNear - zFar) + 1.0);
}
