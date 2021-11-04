#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform float outlineAlpha;
uniform float filledAlpha;
uniform float width;

void main(){
    int intWidth = int(width);
    vec4 center = texture2D(DiffuseSampler, texCoord);

    if (center.a == 0.0) {
        for (int sampleX = -intWidth; sampleX <= intWidth; sampleX++) {
            for (int sampleY = -intWidth; sampleY <= intWidth; sampleY++) {
                vec2 sampleCoord = vec2(float(sampleX), float(sampleY)) * oneTexel;
                vec4 sampleColor = texture2D(DiffuseSampler, texCoord + sampleCoord);
                if (sampleColor.a > 0.0) {
                    center = vec4(sampleColor.rgb, outlineAlpha);
                }
            }
        }
    } else {
        center = vec4(center.rgb, filledAlpha);
    }

    gl_FragColor = center;
}