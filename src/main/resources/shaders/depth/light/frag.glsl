#version 120

varying float v_depth;

void main() {
    gl_FragColor = vec4(vec3(v_depth), 1.0);
}