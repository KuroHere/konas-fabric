#version 120
#extension GL_EXT_gpu_shader4 : enable

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec4 outlineColor;
uniform vec4 filledColor;
uniform float width;
uniform float dotSpacing;
uniform float vertical;
uniform float dotSize;

void main(){
    int intWidth = int(width);
    int intDotSpacing = int(dotSpacing);
    int intVertical = int(vertical);
    vec4 center = texture2D(DiffuseSampler, texCoord);

    if (center.a == 0.0) {
        for (int sampleX = -intWidth; sampleX <= intWidth; sampleX++) {
            for (int sampleY = -intWidth; sampleY <= intWidth; sampleY++) {
                vec2 sampleCoord = vec2(float(sampleX), float(sampleY)) * oneTexel;
                vec4 sampleColor = texture2D(DiffuseSampler, texCoord + sampleCoord);
                if (sampleColor.a > 0.0) {
                    center = vec4(outlineColor.rgb, outlineColor.a);
                }
            }
        }
    } else {
        if (intDotSpacing > 0 && int(gl_FragCoord.x / dotSize) % intDotSpacing == 0 && int(gl_FragCoord.y / dotSize) % intVertical == 0) {
            center = vec4(filledColor.rgb, outlineColor.a);
        } else {
            center = vec4(filledColor.rgb, filledColor.a);
        }
    }

    gl_FragColor = center;
}