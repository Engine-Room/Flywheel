
struct Instance {
    vec2 light;
    vec4 color;
    mat4 transform;
    mat3 normalMat;
};

void vertex(inout Vertex v, Instance i) {
    v.pos = (i.transform * vec4(v.pos, 1.)).xyz;
    v.normal = i.normalMat * v.normal;
    v.color = i.color;
    v.light = i.light;
}
