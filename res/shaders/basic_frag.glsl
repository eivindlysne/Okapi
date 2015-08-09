#version 130

uniform sampler2D diffuse0;
uniform sampler2D diffuse1;
//uniform sampler2D normal;

in vec2 vTexCoord;
in vec3 vColor;

out vec4 fragColor;

void main() {
    //fragColor = texture2D(diffuse0, vTexCoord) * vec4(vColor, 1.0);
    vec4 t0 = texture2D(diffuse0, vTexCoord);
    vec4 t1 = texture2D(diffuse1, vTexCoord);
    fragColor = mix(t0, t1, 0.5) * vec4(vColor, 1.0);
}
