#version 130

uniform sampler2D diffuse;
uniform sampler2D specular;
uniform sampler2D normal;
uniform sampler2D depth;

uniform mat4 invViewProjection;

in vec2 vTexCoord;

out vec4 fragColor;

vec3 worldSpacePosition(float depth) {

    float x = 2.0 * vTexCoord.x - 1.0;
    float y = 2.0 * vTexCoord.y - 1.0;
    float z = 2.0 * depth - 1.0;

    // homogeneous space
    vec4 position = vec4(x, y, z, 1.0) / gl_FragCoord.w;

    // world space
    position = invViewProjection * position;

    // normalized
    return position.xyz / position.w;
}


void main(void) {

    vec3 diffuseColor = texture2D(diffuse, vTexCoord).rgb;
    vec3 specularColor = texture2D(specular, vTexCoord).rgb;
    vec3 encodedNormal = texture2D(normal, vTexCoord).rgb;
    float depthValue = texture2D(depth, vTexCoord).r;

    vec3 position = worldSpacePosition(depthValue);
    vec3 decodedNormal = normalize(2.0 * encodedNormal - vec3(1.0));

    fragColor = vec4(diffuseColor, 1.0);
}