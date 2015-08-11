#version 330 core

struct Transform {
    vec3 position;
    vec4 orientation;
    vec3 scale;
};

vec3 quaternion_rotate(vec4 q, vec3 v) {
    return v + 2.0 * cross(cross(v, q.xyz) + q.w * v, q.xyz);
}

uniform mat4 viewProjection;
uniform Transform transform;

in vec3 position;
in vec2 texCoord;
in vec3 color;
in vec3 normal;

out vec3 vPosition;
out vec2 vTexCoord;
out vec3 vColor;
out vec3 vNormal;
out vec3 vWordSpacePosition;

void main(void) {

    vec3 pos = position;
    pos = transform.position + quaternion_rotate(
        transform.orientation,
        transform.scale * pos
    );

    gl_Position = viewProjection * vec4(pos, 1.0);

    vPosition = position;
    vTexCoord = texCoord;
    vColor = color;
    vNormal = normal;
    vWordSpacePosition = pos;
}
