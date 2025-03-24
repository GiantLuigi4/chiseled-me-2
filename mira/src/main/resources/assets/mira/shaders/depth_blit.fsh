#version 140

in vec2 fragUV;
in vec4 fragColor;

out vec4 finalColor;

uniform mat4 projNear;
uniform mat4 projFar;
uniform sampler2D sampNear;
uniform sampler2D sampFar;
uniform sampler2D texture;

vec4 screenToWorld(mat4 proj, in float depth, in vec2 uv){
    vec4 coord = vec4(uv, depth, 1.0) * 2.0 - 1.0;

    coord = inverse(proj) * coord;
    coord /= coord.w; // linearize
    return coord;
}

void main() {
    float depthNear = texture2D(sampNear, fragUV).r;
    float depthFar = texture2D(sampFar, fragUV).r;

    vec4 farForNear = screenToWorld(projNear, 1.0, fragUV);
    vec4 crdNear = screenToWorld(projNear, depthNear, fragUV);
    vec4 nearForFar = screenToWorld(projFar, 0.0125, fragUV);
    vec4 crdFar = screenToWorld(projFar, depthFar, fragUV);

    //    finalColor = vec4(abs(normalize(crdFar.xyz)), 1.0);
    //    finalColor = vec4(abs(crdFar.zzz), 1.0);
    //    finalColor = vec4(abs(normalize(crdNear.xyz)), 1.0);
    //    finalColor = vec4(abs(crdNear.zzz), 1.0);

    if (farForNear.z == crdNear.z) {
        crdNear.z = 1.0;
    }

    if (min(abs(crdNear.z), abs(crdFar.z)) < abs(nearForFar.z)) {
        if (abs(crdNear.z) >= abs(crdFar.z) - 0.0001) {
            discard;
        }
        finalColor = texture2D(texture, fragUV);
    } else {
        discard;
    }
}
