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
    // compute depth values for near patch
    float depthNear = texture2D(sampNear, fragUV).r;

    vec4 farForNear = projFar * screenToWorld(projNear, 1.0, fragUV);
    vec4 crdNear = projFar * screenToWorld(projNear, depthNear, fragUV);

    farForNear /= farForNear.w;
    crdNear /= crdNear.w;

    // pre-fail anything that wasn't rendered in the near-patch
    if (farForNear.z == crdNear.z) {
        discard;
    }

    // compute depth values for far patch
    float depthFar = texture2D(sampFar, fragUV).r;

    vec4 nearForFar = projFar * screenToWorld(projFar, 0.0125, fragUV);
    vec4 crdFar = projFar * screenToWorld(projFar, depthFar, fragUV);

    nearForFar /= nearForFar.w;
    crdFar /= crdFar.w;

    if (crdNear.z < crdFar.z) {
        finalColor = texture2D(texture, fragUV);

        vec4 crdFarPrecision = projFar * screenToWorld(projFar, depthFar + 0.01, fragUV);
        crdFarPrecision /= crdFarPrecision.w;

        if (abs(crdNear.z - crdFar.z) < abs(crdFarPrecision.z - crdFar.z)) {
            discard;
        }
    } else {
        discard;
    }
}
