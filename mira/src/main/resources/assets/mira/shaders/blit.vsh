#version 120

varying vec2 fragUV;
varying vec4 fragColor;

void main() {
    gl_Position = ftransform();
    fragUV = gl_MultiTexCoord0.xy;
    fragColor = gl_Color;
}
