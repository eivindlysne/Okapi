#version 130

uniform sampler2D gPositionDepth;
uniform sampler2D gNormal;
uniform sampler2D noiseTex;

uniform vec3 samples[64];

uniform mat4 projection;

// FIXME: Hardcoded screen dimensions
const vec2 noiseScale = vec2(960.0/4.0, 540.0/4.0);
int kernelSize = 64;
float radius = 1.0;

in vec2 vTexCoord;

out float fragColor;

void main(void) {

    vec3 fragPos = texture2D(gPositionDepth, vTexCoord).xyz;
    vec3 normal = texture2D(gNormal, vTexCoord).rgb;
    vec3 randomVec = texture2D(noiseTex, vTexCoord * noiseScale).xyz;

    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 TBN = mat3(tangent, bitangent, normal);

    // Gramm-Schmidt
    float occlusion = 0.0;


    for (int i = 0; i < kernelSize; ++i) {

        vec3 samplePos = TBN * samples[i];
        samplePos = fragPos + samplePos * radius;

        vec4 offset = vec4(samplePos, 1.0);
        offset = projection * offset;
        offset.xyz /= offset.w;
        offset.xyz = offset.xyz * 0.5 + 0.5;

        float sampleDepth = -texture2D(gPositionDepth, offset.xy).w;

        float rangeCheck = smoothstep(0.0, 1.0, radius / abs(fragPos.z - sampleDepth));
        occlusion += (sampleDepth >= samplePos.z ? 1.0 : 0.0) * rangeCheck;
    }

    occlusion = 1.0 - (occlusion / kernelSize);

    fragColor = occlusion;
    //fragColor = vec4(occlusion, occlusion, occlusion, 1.0);
}