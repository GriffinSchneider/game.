
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

uniform vec3 iResolution;
uniform vec2 cente;
uniform float iGlobalTime;
uniform sampler2D palatte;
uniform float palatteSize;

float saw(float x) {
  return abs( mod( x, 2.0 ) - 1.0);
}

float smoothsaw(float x) {
  return smoothstep( 0.0, 1.0, saw( x ));
}

void main(void) {
  // vec2 center = iResolution.xy * 0.5;

  vec2 center = cente;
  // center = vec2(smoothsaw(iGlobalTime*0.6) * center.x,
  // smoothsaw(iGlobalTime*0.6 + 0.4) * center.y);

  MEDIUMP vec2 v = gl_FragCoord.xy - center;
  
  float dist = length(v);
  
  dist += 15.0 * saw(4.25 * 3.14 * atan(v.y, v.x));

  float d = mod(dist*0.03 - iGlobalTime*10.0, palatteSize);
  
  float palatteIndex = d/palatteSize;

  vec4 col = texture2D(palatte, vec2(palatteIndex, 0.0));

  gl_FragColor = vec4(col.x, col.y, col.z, 1 );
}
