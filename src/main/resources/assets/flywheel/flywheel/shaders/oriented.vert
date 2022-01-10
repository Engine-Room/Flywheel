#use "flywheel:core/quaternion.glsl"

struct Oriented {
    vec2 light;
    vec4 color;
    vec3 pos;
    vec3 pivot;
    vec4 rotation;
};

void vertex(inout Vertex v, Oriented o) {
    v.pos = rotateVertexByQuat(v.pos - o.pivot, o.rotation) + o.pivot + o.pos;
    v.normal = rotateVertexByQuat(v.normal, o.rotation);
    v.color = o.color;
    v.light = o.light;
}
