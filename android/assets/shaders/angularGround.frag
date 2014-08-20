
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

uniform float iGlobalTime;
uniform vec3 iResolution;
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
  iResolution;
  iGlobalTime;
  
  vec2 offset = vec2(xOffset, yOffset);
  vec2 thing = gl_FragCoord.xy + offset;
  vec2 uv = thing / iResolution.xy;

  uv.y += sin((iGlobalTime*0.05+uv.x)*20.)*0.01;
  uv.x += sin((iGlobalTime*0.04+uv.y)*20.)*0.01;
    
  float index = uv.y*20.;
  index += saw(uv.x*20.);
  index = mod(index, 1.);
    
  gl_FragColor = 
    mix(vec4(0.363,0.843,0.098,1),
        vec4(0.445,0.969,0.152,1), 
        index);
}
