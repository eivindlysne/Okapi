#version 130

out vec4 gPositionDepth;
out vec3 gNormal;
out vec4 gAlbedoSpec;

in vec3 vFragPosition;
in vec2 vTexCoord;
in vec3 vColor;
in vec3 vNormal;

uniform sampler2D diffuseTex;

const float NEAR = 0.01; // projection matrix's near plane
const float FAR = 256.0f; // projection matrix's far plane
float linearize_depth(float depth) {
    float z = depth * 2.0 - 1.0; // Back to NDC
    return (2.0 * NEAR * FAR) / (FAR + NEAR - z * (FAR - NEAR));
}

void main(void) {

    gPositionDepth.xyz = vFragPosition;
    gPositionDepth.a = linearize_depth(gl_FragCoord.z);

    gNormal = normalize(vNormal);

    // Specular can be stored in gAlbedoSpec.a
    gAlbedoSpec.rgb = texture2D(diffuseTex, vTexCoord).rgb * vColor;
    gAlbedoSpec.a = 1.0;
    //gAlbedoSpec.rgb = vec3(0.95);
}
