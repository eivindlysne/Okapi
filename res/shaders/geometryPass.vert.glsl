#version 130

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

in vec3 position;
in vec2 texCoord;
in vec3 color;
in vec3 normal;

out vec3 vFragPosition;
out vec2 vTexCoord;
out vec3 vColor;
out vec3 vNormal;

void main(void) {

    vec4 viewPosition = view * model * vec4(position, 1.0);

    gl_Position = projection * viewPosition;

    vFragPosition = viewPosition.xyz;
    vTexCoord = texCoord;
    vColor = color;
    vNormal = transpose(inverse(mat3(view * model))) * normal;
}
