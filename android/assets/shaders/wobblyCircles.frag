
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
uniform vec3 iResolution;
uniform sampler2D palatte;
uniform float palatteSize;

uniform sampler2D u_texture;

float saw(float x) {
  return abs( mod( x, 2.0 ) - 1.0);
}

float smoothsaw(float x) {
  return smoothstep( 0.0, 1.0, saw( x ));
}

#define ANTI_ALIAS_AMOUNT 0.005

// void main(void)
// {
//   u_texture;
//   iGlobalTime;
//   palatte;
//   palatteSize;
//   iResolution;
//   vec2 uv = gl_FragCoord.xy / iResolution.xy;
	
//   float xForFunc1 = uv.x;
//   xForFunc1 += iGlobalTime/4.;
	
//   float sawed1 = saw(xForFunc1 * 4.0);
//   float stepped1 = smoothstep(uv.y, uv.y+ANTI_ALIAS_AMOUNT, sawed1);
	
//   float xForFunc2 = uv.x;
//   xForFunc2 += iGlobalTime/2.;
//   float sawed2 = saw(xForFunc2 * 3.0);
//   float stepped2 = smoothstep(uv.y, uv.y+ANTI_ALIAS_AMOUNT, sawed2);
	
//   float xForFunc3 = uv.x;
//   xForFunc3 += iGlobalTime/3.;
//   float sawed3 = saw(xForFunc3 * 5.0);
//   float stepped3 = smoothstep(uv.y, uv.y+ANTI_ALIAS_AMOUNT, sawed3);
	
//   gl_FragColor = vec4(stepped1,stepped2,stepped3,1.0);
// }

void main(void) {
  u_texture;
  iGlobalTime;
  palatte;
  palatteSize;
  iResolution;

  MEDIUMP vec2 v = gl_FragCoord.xy - center;
  
  float dist = length(v);
  
  dist += 30.0 * saw(4.25 * 3.14 * atan(v.y, v.x));

  float d = mod(dist*0.03 -       iGlobalTime*10.0, palatteSize);
  
  float palatteIndex1 = d / palatteSize;
  float palatteIndex2 = mod(d+1.0, palatteSize) / palatteSize;

  vec4 col1 = texture2D(palatte, vec2(palatteIndex1, 0.0));
  vec4 col2 = texture2D(palatte, vec2(palatteIndex2, 0.0));

  vec4 col = mix(col1, col2, smoothstep(0.0, 1.0, fract(d)));

  gl_FragColor = vec4(col.x, col.y, col.z, 1 );
}
