
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
uniform float iGlobalTime;
uniform sampler2D u_texture;

void main(void) {
  iResolution;
  iGlobalTime;
  u_texture;
  
  vec2 p=gl_FragCoord.xy-iResolution.xy*.5;
  gl_FragColor = sin(vec4(2,4,8,0)*(dot(p,p)-iGlobalTime));
}
