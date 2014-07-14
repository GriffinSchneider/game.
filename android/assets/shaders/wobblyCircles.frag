
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

uniform vec2 center;
uniform float iGlobalTime;
uniform sampler2D palatte;
uniform float palatteSize;

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
  palatte;
  palatteSize;

  MEDIUMP vec2 v = gl_FragCoord.xy - center;
  
  float dist = length(v);
  
  dist += 15.0 * saw(4.25 * 3.14 * atan(v.y, v.x));

  float d = mod(dist*0.03 - iGlobalTime*10.0, palatteSize);
  
  float palatteIndex = d/palatteSize;

  vec4 col = texture2D(palatte, vec2(palatteIndex, 0.0));

  gl_FragColor = vec4(col.x, col.y, col.z, 1 );
}
