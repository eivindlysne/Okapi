#version 130

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
};

struct PointLight {
    vec3 color;
    float intensity;
    vec3 position;
    Attenuation attenuation;
    float range;
};

uniform sampler2D diffuse;
uniform sampler2D specular;
uniform sampler2D normal;
uniform sampler2D depth;
uniform sampler2D ssao;

uniform mat4 invViewProjection;

uniform PointLight light;

in vec2 vTexCoord;

out vec4 fragColor;

vec3 worldSpacePosition(float depth) {

    // screen space
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

float square(float x) {
    return x * x;
}


vec4 calculatePointLight(vec3 surfaceToLight, float lightDistance, vec3 normal) {

    float diffuseCoefficient = max(0.0, dot(normal, surfaceToLight));

    float attenuation = (
        light.attenuation.constant +
        light.attenuation.linear * lightDistance +
        light.attenuation.quadratic * lightDistance * lightDistance);

    attenuation = 1.0 / attenuation;
    attenuation *= clamp(
        square(1.0 - square(square(lightDistance / light.range))), 0, 1);

    vec3 diffuse = diffuseCoefficient * (light.color * light.intensity) * attenuation;
    return vec4(diffuse, 1.0);
}


void main(void) {

    vec3 diffuseColor = texture2D(diffuse, vTexCoord).rgb;
    vec3 specularColor = texture2D(specular, vTexCoord).rgb;
    vec3 encodedNormal = texture2D(normal, vTexCoord).rgb;
    float depthValue = texture2D(depth, vTexCoord).r;

    float ao = texture2D(ssao, vTexCoord).r;
    vec3 ambient = vec3(0.3 * ao);

    vec3 position = worldSpacePosition(depthValue);
    vec3 decodedNormal = normalize(2.0 * encodedNormal - vec3(1.0));

    vec3 surfaceToLight = normalize(light.position - position);
    float lightDistance = length(light.position - position);
    vec4 lightColor = calculatePointLight(surfaceToLight, lightDistance, decodedNormal);

    fragColor = vec4(ambient + (lightColor.rgb * diffuseColor), lightColor.a);
}