#use "flywheel:uniform/view.glsl"

layout(location = 0) in vec3 worldPos;

void main() {
    gl_Position = flw_viewProjection * vec4(worldPos, 1.0);
}
