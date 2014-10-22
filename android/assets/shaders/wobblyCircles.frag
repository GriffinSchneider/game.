
#ifdef GL_ES
precision highp float;
#define HIGHP highp
#define LOWP lowp
#define MEDIUMP mediump
#else
#define HIGHP ;
#define LOWP ;
#define MEDIUMP ;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform float iGlobalTime;
uniform vec3 iResolution;
uniform sampler2D palatte;
uniform float palatteSize;

uniform float xOffset;
uniform float yOffset;

uniform sampler2D u_texture;

float saw(float x) {
  return abs( mod( x, 2.0 ) - 1.0);
}

float smoothsaw(float x) {
  return smoothstep( 0.0, 1.0, saw( x ));
}

void main(void) {
  u_texture;
  iGlobalTime;
  iResolution;
  palatte;
  palatteSize;

  vec2 offset = vec2(xOffset, yOffset);
  vec2 thing = gl_FragCoord.xy + offset;
  vec2 uv = thing;// / iResolution.xy;

  float dist = length(uv);
  
  dist += 30.0 * saw(4.25 * 3.14 * atan(uv.y, uv.x));

  float d = mod(dist*0.03 -  iGlobalTime*10.0, palatteSize);
  
  float palatteIndex1 = d / palatteSize;
  float palatteIndex2 = mod(d+1.0, palatteSize) / palatteSize;

  vec4 col1 = texture2D(palatte, vec2(palatteIndex1, 0.0));
  vec4 col2 = texture2D(palatte, vec2(palatteIndex2, 0.0));
  
  vec4 col = mix(col1, col2, smoothstep(0.0, 1.0, fract(d)));
  gl_FragColor = vec4(col.x, col.y, col.z, 1 );
}

