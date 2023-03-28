layout(location = 0) in vec3 worldPos;

void main() {
    gl_Position = flywheel.viewProjection * vec4(worldPos, 1.0);
}
