
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

uniform vec3 iResolution;

uniform sampler2D u_texture;

float saw(float x) {
  return abs( mod( x, 2.0 ) - 1.0);
}

float smoothsaw(float x) {
  return smoothstep( 0.0, 1.0, saw( x ));
}

void main(void) {
  u_texture;
  
  vec2 uv = gl_FragCoord.xy / iResolution.xy;
    
  float index = uv.y*10.;
  index += saw(uv.x*10.);
  index = mod(index, 1.);
    
  gl_FragColor = 
    mix(vec4(0.363,0.843,0.098,1),
        vec4(0.445,0.969,0.152,1), 
        index);
}
