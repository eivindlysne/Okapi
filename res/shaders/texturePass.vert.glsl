#version 130

uniform vec3 scaleFactor = vec3(1.0, 1.0, 1.0);

in vec3 position;
in vec2 texCoord;

out vec2 vTexCoord;


void main(void) {

    gl_Position = vec4(scaleFactor * position, 1.0);

    vTexCoord = texCoord;
}
