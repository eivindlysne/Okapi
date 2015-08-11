#version 130

struct Transform {
    vec3 position;
    vec4 orientation;
    vec3 scale;
};

vec3 quaternion_rotate(vec4 q, vec3 v) {
    return v + 2.0 * cross(cross(v, q.xyz) + q.w * v, q.xyz);
}

struct Material {
    sampler2D diffuseMap;
    vec4 diffuseColor;
    vec4 specularColor;
    float specularExponent;
};


uniform Transform transform;

uniform sampler2D diffuse0;

in vec3 vPosition;
in vec2 vTexCoord;
in vec3 vColor;
in vec3 vNormal;
in vec3 vWordSpacePosition;

out vec4 gDiffuseColor;
out vec4 gSpecularColor;
out vec4 gNormals;

void main(void) {

    vec4 diffuseColor = texture2D(diffuse0, vTexCoord);
    vec3 surfaceColor = diffuseColor.rgb * vColor;

    vec3 normal = normalize(quaternion_rotate(transform.orientation, vNormal));

    gDiffuseColor = vec4(surfaceColor, 1.0);
    gSpecularColor = vec4(1, 1, 1, 1.0 / 4.0); // TODO: Materials 4.0==specularExponent
    gNormals = vec4(0.5 * (normal + vec3(1.0)), 1.0);
}
