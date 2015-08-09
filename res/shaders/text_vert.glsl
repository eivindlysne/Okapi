#version 130

uniform mat4 viewProjection;

in vec3 position;
in vec2 tex_coord;
in vec3 color;

out vec2 vTexCoord;
out vec3 vColor;

void main() {
    vTexCoord = tex_coord;
    vColor = color;
    gl_Position = viewProjection * vec4(position, 1.0);
}
