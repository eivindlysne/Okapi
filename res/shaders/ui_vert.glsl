#version 130


uniform mat4 viewProjection;

in vec3 position;
//in vec4 color;

//out vec4 vColor;

void main() {
    //vColor = color;

    gl_Position = viewProjection * vec4(position, 1.0);
}