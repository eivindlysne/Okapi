#version 130

struct Transform {
    vec3 position;
    vec4 orientation;
    vec3 scale;
};

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
};

struct PointLight {
    vec3 position;
    vec3 intensity;
    Attenuation attenuation;
};

vec3 quaternion_rotate(vec4 q, vec3 v) {
    return v + 2.0 * cross(cross(v, q.xyz) + q.w * v, q.xyz);
}

float light_range(Attenuation a, float i) {

    return -a.linear + sqrt(
        a.linear * a.linear -
        4.0 * a.quadratic * (a.constant - 256.0 * i)); // 256 = color depth
}

// Lights
uniform vec4 ambientLight = vec4(1, 1, 1, 0.5);
uniform PointLight pointLight;

// Position
uniform vec3 cameraPosition;
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
    vec3 normal = normalize(quaternion_rotate(transform.orientation, vNormal));

    vec3 surfaceToLight = normalize(pointLight.position - vWordSpacePosition);

    float diffuseCoefficient = max(0.0, dot(normal, surfaceToLight));


    // Specularity calculation
    // incidence = -surfaceToLight;
    // reflection = reflect(incidence, normal);
    // cosTheta = max(0.0, dot(surfaceToCamera, reflection));

    vec3 surfaceToCamera = normalize(cameraPosition - vWordSpacePosition);
    // NOTE: branching is bad but we need it.
    //       Compiler will hopefully optimize it away
    float specularCoefficient = diffuseCoefficient > 0.0 ?
        pow(max(0.0, dot(surfaceToCamera, reflect(-surfaceToLight, normal))),
            100.0) :
        0.0;

    // Attenuation calculation
    // FIXME: Something is a little off. Comment out ambiance to see
    float lightDistance = length(pointLight.position - vWordSpacePosition);
    float attenuation =
        pointLight.attenuation.constant +
        pointLight.attenuation.linear * lightDistance +
        pointLight.attenuation.quadratic * lightDistance * lightDistance;
    attenuation = 1.0 / attenuation;
    float r = light_range(pointLight.attenuation, 1.0);
    attenuation *= clamp(
        pow(1 - pow(lightDistance / r, 4.0), 2.0),
        0, 1);

    vec3 diffuse = diffuseCoefficient * pointLight.intensity;
    vec3 ambient = ambientLight.rgb * ambientLight.a;
    vec3 specular = specularCoefficient * pointLight.intensity;

    vec3 finalColor = (ambient + (diffuse + specular) * attenuation)
                      * surfaceColor;

    // Note: Not sure we need gamma
//    vec3 gamma = vec3(1.0 / 2.2);
//    finalColor = pow(finalColor, gamma);

    fragColor = vec4(finalColor, 1.0);
}
