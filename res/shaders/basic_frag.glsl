#version 130

struct Transform {
    vec3 position;
    vec4 orientation;
    vec3 scale;
};

struct PointLight {
    vec3 position;
    vec3 intensity;
};

vec3 quaternion_rotate(vec4 q, vec3 v) {
    return v + 2.0 * cross(cross(v, q.xyz) + q.w * v, q.xyz);
}

uniform vec4 ambientLight = vec4(1, 1, 1, 0.08);

// Lights
uniform PointLight pointLight = PointLight(vec3(0), vec4(0));

uniform Transform transform;

// Textures
uniform sampler2D diffuse0;
uniform sampler2D diffuse1;
//uniform sampler2D normal;

in vec3 vPosition;
in vec2 vTexCoord;
in vec3 vColor;
in vec3 vNormal;
in vec3 vWordSpacePosition;

out vec4 fragColor;

void main() {

//    // NOTE: Uncomment for mixing textures
//    vec4 t0 = texture2D(diffuse0, vTexCoord);
//    vec4 t1 = texture2D(diffuse1, vTexCoord);
//    fragColor = mix(t0, t1, 0.5) * vec4(vColor, 1.0);

    vec4 diffuseColor = texture2D(diffuse0, vTexCoord);
    vec3 surfaceColor = diffuseColor.rgb * vColor;

    // Rotate the normal vector according to transform.orientation.
    // Then inverte them due to facing?
    vec3 normal = -normalize(quaternion_rotate(transform.orientation, vNormal));

    vec3 surfaceToLight = normalize(pointLight.position - vWordSpacePosition);
    float lightDistance = length(pointLight.position - vWordSpacePosition); // surfaceToLight


    float diffuseCoefficient = max(0.0, dot(normal, surfaceToLight));
    diffuseCoefficient /= 1 + (lightDistance * lightDistance);


    vec3 diffuse = diffuseCoefficient * pointLight.intensity * surfaceColor;
    vec3 ambient = (ambientLight.rgb * ambientLight.a) * surfaceColor;


    vec3 finalColor = ambient + diffuse;

    // Note: Not sure we need gamma
    vec3 gamma = vec3(1.0 / 2.2);
    finalColor = pow(finalColor, gamma);

    fragColor = vec4(finalColor, 1.0);
}
