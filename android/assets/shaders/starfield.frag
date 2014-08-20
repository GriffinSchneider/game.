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

uniform sampler2D previousFrame;

uniform float parallaxX;
uniform float parallaxY;

// // Star Nest by Pablo Román Andrioli
// // This content is under the MIT License.

// // Original Values:
// // #define ITERATIONS 17
// // #define FORMUPARAM 0.53

// // #define VOLSTEPS 20
// // #define STEPSIZE 0.1

// // #define ZOOM   0.800
// // #define TILE   0.850
// // #define SPEED  0.010 

// // #define BRIGHTNESS 0.0015
// // #define DARKMATTER 0.300
// // #define DISTFADING 0.730
// // #define SATURATION 0.850

// #define ITERATIONS 17
// #define FORMUPARAM 0.7

// #define VOLSTEPS 10
// #define STEPSIZE 0.13

// #define ZOOM   0.800
// #define TILE   0.850
// #define SPEED  0.010 

// #define BRIGHTNESS 0.0015
// #define DARKMATTER 0.300
// #define DISTFADING 0.730
// #define SATURATION 0.850


// void main(void) {

//   parallaxX;
//   parallaxY;
//   previousFrame;
  
//   //get coords and direction
//   vec2 uv = gl_FragCoord.xy/iResolution.xy - 0.5;
//   uv.y *= iResolution.y / iResolution.x;
//   vec3 dir = vec3(uv * ZOOM, 1.0);
//   float time = iGlobalTime*SPEED + 0.25;

//   //mouse rotation
//   // float a1=.5+iMouse.x/iResolution.x*2.;
//   // float a2=.8+iMouse.y/iResolution.y*2.;
//   // mat2 rot1=mat2(cos(a1),sin(a1),-sin(a1),cos(a1));
//   // mat2 rot2=mat2(cos(a2),sin(a2),-sin(a2),cos(a2));
//   // dir.xz*=rot1;
//   // dir.xy*=rot2;
//   vec3 from = vec3(1.0, 0.5, 0.5);
        
//   // Apply parallax
//   from+=vec3(parallaxX, parallaxY, -2.0);
        
//   // from.xz*=rot1;
//   // from.xy*=rot2;
	
//   //volumetric rendering
//   float s = 0.1;
//   float fade = 1.0;
//   vec3 v = vec3(0.0);
//   for (int r = 0; r < VOLSTEPS; r++) {
//     vec3 p = from + s*dir*0.5;
//     p = abs(vec3(TILE) - mod(p, vec3(TILE * 2.0))); // tiling fold
//     float pa = 0.0;
//     float a = 0.0;
//     for (int i = 0; i < ITERATIONS; i++) { 
//       p = abs(p)/dot(p,p) - FORMUPARAM; // the magic formula
//       a += abs(length(p) - pa); // absolute sum of average change
//       pa = length(p);
//     }
//     float dm = max(0.0, DARKMATTER - a*a*.001); //dark matter
//     a *= a*a; // add contrast
//     if (r>6) {
//       fade *= 1.0 - dm; // dark matter, don't render near
//     }
//     //v+=vec3(dm,dm*.5,0.);
//     v += fade;
//     v += vec3(s, s*s, s*s*s*s) * a * BRIGHTNESS * fade; // coloring based on distance
//     fade *= DISTFADING; // distance fading
//     s += STEPSIZE;
//   }
//   v = mix(vec3(length(v)), v, SATURATION); //color adjust
//   gl_FragColor = vec4(v * 0.01, 1.0);	


//   // vec4 oldValue = texture2D(previousFrame, gl_FragCoord.xy/iResolution.xy);
//   // gl_FragColor.rgb = mix(oldValue.rgb - 0.004, gl_FragColor.rgb, 0.3);
//   // gl_FragColor = oldValue;
// }

//CBS
//Parallax scrolling fractal galaxy.
//Inspired by JoshP's Simplicity shader: https://www.shadertoy.com/view/lslGWr

// http://www.fractalforums.com/new-theories-and-research/very-simple-formula-for-fractal-patterns/
float field(in vec3 p,float s) {
  float strength = 7. + .03 * log(1.e-6 + fract(sin(iGlobalTime) * 4373.11));
  float accum = s/4.;
  float prev = 0.;
  float tw = 0.;
  for (int i = 0; i < 26; ++i) {
    float mag = dot(p, p);
    p = abs(p) / mag + vec3(-.5, -.4, -1.5);
    float w = exp(-float(i) / 7.);
    accum += w * exp(-strength * pow(abs(mag - prev), 2.2));
    tw += w;
    prev = mag;
  }
  return max(0., 5. * accum / tw - .7);
}

// Less iterations for second layer
float field2(in vec3 p, float s) {
  float strength = 7. + .03 * log(1.e-6 + fract(sin(iGlobalTime) * 4373.11));
  float accum = s/4.;
  float prev = 0.;
  float tw = 0.;
  for (int i = 0; i < 18; ++i) {
    float mag = dot(p, p);
    p = abs(p) / mag + vec3(-.5, -.4, -1.5);
    float w = exp(-float(i) / 7.);
    accum += w * exp(-strength * pow(abs(mag - prev), 2.2));
    tw += w;
    prev = mag;
  }
  return max(0., 5. * accum / tw - .7);
}

vec3 nrand3( vec2 co )
{
  vec3 a = fract( cos( co.x*8.3e-3 + co.y )*vec3(1.3e5, 4.7e5, 2.9e5) );
  vec3 b = fract( sin( co.x*0.3e-3 + co.y )*vec3(8.1e5, 1.0e5, 0.1e5) );
  vec3 c = mix(a, b, 0.5);
  return c;
}


void main() {
  vec2 uv = 2. * gl_FragCoord.xy / iResolution.xy - 1.;
  vec2 uvs = uv * iResolution.xy / max(iResolution.x, iResolution.y);
  vec3 p = vec3(uvs / 4., 0) + vec3(1., -1.3, 0.);
  p += .2 * vec3(parallaxX*20., parallaxY*6., 0.0); //sin(iGlobalTime / 128.));
	
  float freqs[4];
  //Sound
  freqs[0] = 1.0;
  freqs[1] = 0.6;
  freqs[2] = 0.5;
  freqs[3] = 0.4;

  float t = field(p,freqs[2]);
  float v = (1. - exp((abs(uv.x) - 1.) * 6.)) * (1. - exp((abs(uv.y) - 1.) * 6.));
	
  //Second Layer
  vec3 p2 = vec3(uvs / (4.+sin(iGlobalTime*0.11)*0.2+0.2+sin(iGlobalTime*0.15)*0.3+0.4), 1.5) + vec3(2., -1.3, -1.);
  p2 += 0.25 * vec3(parallaxX*40., parallaxY*10., sin(iGlobalTime / 128.));
  float t2 = field2(p2,freqs[3]);
  vec4 c2 = mix(.4, 1., v) * vec4(1.3 * t2 * t2 * t2 ,1.8  * t2 * t2 , t2* freqs[0], t2);
	
	
  //Let's add some stars
  //Thanks to http://glsl.heroku.com/e#6904.0
  vec2 seed = p.xy * 2.0;	
  seed = floor(seed * iResolution.x);
  vec3 rnd = nrand3( seed );
  vec4 starcolor = vec4(pow(rnd.y,40.0));
	
  //Second Layer
  vec2 seed2 = p2.xy * 2.0;
  seed2 = floor(seed2 * iResolution.x);
  vec3 rnd2 = nrand3( seed2 );
  starcolor += vec4(pow(rnd2.y,40.0));
	
  gl_FragColor = mix(freqs[3]-.3, 1., v) * vec4(1.5*freqs[2] * t * t* t , 1.2*freqs[1] * t * t, freqs[3]*t, 1.0)+c2+starcolor;
}
