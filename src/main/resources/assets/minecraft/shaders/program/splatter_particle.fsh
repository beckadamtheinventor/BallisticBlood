#version 120

uniform sampler2D DiffuseSampler; // framebuffer
uniform sampler2D ParticleAtlas;

varying vec2 texCoord;
varying vec2 oneTexel;

#define modulated(f,d) (f < 0.5 ? (2.0 * f * d) : (1.0 - 2.0 * (1.0 - f) * (1.0 - d)))

void main() {
//    vec4 dcolor = texture2D(DiffuseSampler, texCoord);
//    vec4 fcolor = texture2D(ParticleAtlas, texCoord);
//    fcolor.r = modulated(fcolor.r, dcolor.r);
//    fcolor.g = modulated(fcolor.g, dcolor.g);
//    fcolor.b = modulated(fcolor.b, dcolor.b);
    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
