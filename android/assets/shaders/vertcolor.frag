
varying vec4 v_color;
varying vec2 v_texCoords;

uniform vec3 iResolution;
uniform float iGlobalTime;
uniform sampler2D u_texture;

void main(void) {
  iResolution;
  iGlobalTime;
  u_texture;
  
  // gl_FragColor = texture2D(u_texture, v_texCoords);
  gl_FragColor = v_color;
}
