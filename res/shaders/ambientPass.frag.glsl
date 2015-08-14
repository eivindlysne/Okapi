#version 130

in vec2 vTexCoord;

out vec4 fragColor;

uniform sampler2D gPositionDepth;
uniform sampler2D gNormal;
uniform sampler2D gAlbedoSpec;
uniform sampler2D ssaoTex;


struct PointLight {
    vec3 position;
    vec3 color;

    float constant;
    float linear;
    float quadratic;
};
uniform PointLight light = PointLight(
    vec3(0,4,0), vec3(0.2, 0.2, 0.7), 1.0, 0.09, 0.032
);

void main(void) {

// Retrieve data from gbuffer
    vec3 FragPos = texture(gPositionDepth, vTexCoord).rgb;
    vec3 Normal = texture(gNormal, vTexCoord).rgb;
    vec3 Diffuse = texture(gAlbedoSpec, vTexCoord).rgb;
    float Depth = texture(gPositionDepth, vTexCoord).a;
    float AmbientOcclusion = texture(ssaoTex, vTexCoord).r;

    // Then calculate lighting as usual
    vec3 ambient = vec3(0.3 * AmbientOcclusion);
    vec3 lighting  = ambient;
    vec3 viewDir  = normalize(-FragPos); // Viewpos is (0.0.0)
    // Diffuse
    vec3 lightDir = normalize(light.position - FragPos);
    vec3 diffuse = max(dot(Normal, lightDir), 0.0) * Diffuse * light.color;
    // Specular
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(Normal, halfwayDir), 0.0), 8.0);
    vec3 specular = light.color * spec;
    // Attenuation
    float distance = length(light.position - FragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * distance * distance);
    diffuse *= attenuation;
    specular *= attenuation;
    lighting += diffuse + specular;


    fragColor = vec4(lighting * Diffuse, 1.0);
    //fragColor = vec4(ambient + Diffuse, 1.0);
}