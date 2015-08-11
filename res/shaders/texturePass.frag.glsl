#version 130


uniform sampler2D texture0;

in vec2 vTexCoord;

out vec4 fragColor;

void main(void) {

    fragColor = texture2D(texture0, vTexCoord);
}
