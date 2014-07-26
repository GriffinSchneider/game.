// #extension GL_OES_standard_derivatives : enable

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

uniform float parallaxX;
uniform float parallaxY;

// Star Nest by Pablo Román Andrioli
// This content is under the MIT License.

// Original Values:
// #define ITERATIONS 17
// #define FORMUPARAM 0.53

// #define VOLSTEPS 20
// #define STEPSIZE 0.1

// #define ZOOM   0.800
// #define TILE   0.850
// #define SPEED  0.010 

// #define BRIGHTNESS 0.0015
// #define DARKMATTER 0.300
// #define DISTFADING 0.730
// #define SATURATION 0.850

#define ITERATIONS 17
#define FORMUPARAM 0.7

#define VOLSTEPS 10
#define STEPSIZE 0.13

#define ZOOM   0.800
#define TILE   0.850
#define SPEED  0.010 

#define BRIGHTNESS 0.0015
#define DARKMATTER 0.300
#define DISTFADING 0.730
#define SATURATION 0.850


void main(void) {

  parallaxX;
  parallaxY;
  
  //get coords and direction
  vec2 uv = gl_FragCoord.xy/iResolution.xy - 0.5;
  uv.y *= iResolution.y / iResolution.x;
  vec3 dir = vec3(uv * ZOOM, 1.0);
  float time = iGlobalTime*SPEED + 0.25;

  //mouse rotation
  // float a1=.5+iMouse.x/iResolution.x*2.;
  // float a2=.8+iMouse.y/iResolution.y*2.;
  // mat2 rot1=mat2(cos(a1),sin(a1),-sin(a1),cos(a1));
  // mat2 rot2=mat2(cos(a2),sin(a2),-sin(a2),cos(a2));
  // dir.xz*=rot1;
  // dir.xy*=rot2;
  vec3 from = vec3(1.0, 0.5, 0.5);
        
  // Apply parallax
  from+=vec3(parallaxX, parallaxY, -2.0);
        
  // from.xz*=rot1;
  // from.xy*=rot2;
	
  //volumetric rendering
  float s = 0.1;
  float fade = 1.0;
  vec3 v = vec3(0.0);
  for (int r = 0; r < VOLSTEPS; r++) {
    vec3 p = from + s*dir*0.5;
    p = abs(vec3(TILE) - mod(p, vec3(TILE * 2.0))); // tiling fold
    float pa = 0.0;
    float a = 0.0;
    for (int i = 0; i < ITERATIONS; i++) { 
      p = abs(p)/dot(p,p) - FORMUPARAM; // the magic formula
      a += abs(length(p) - pa); // absolute sum of average change
      pa = length(p);
    }
    float dm = max(0.0, DARKMATTER - a*a*.001); //dark matter
    a *= a*a; // add contrast
    if (r>6) {
      fade *= 1.0 - dm; // dark matter, don't render near
    }
    //v+=vec3(dm,dm*.5,0.);
    v += fade;
    v += vec3(s, s*s, s*s*s*s) * a * BRIGHTNESS * fade; // coloring based on distance
    fade *= DISTFADING; // distance fading
    s += STEPSIZE;
  }
  v = mix(vec3(length(v)), v, SATURATION); //color adjust
  gl_FragColor = vec4(v * 0.01, 1.0);	
}
