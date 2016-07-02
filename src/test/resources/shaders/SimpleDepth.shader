{"name":"SimpleDepth","desc":"Creates a depthmap originating from the camera.","vertex":"#version 120\n\nattribute vec4 a_position;\n\nuniform mat4 u_projViewTrans;\nuniform mat4 u_worldTrans;\nuniform vec2 u_cameraNearFar;\n\nvarying float v_depth;\n\nvoid main() {\n    gl_Position \u003d u_projViewTrans * (u_worldTrans * a_position);\n\n    v_depth \u003d gl_Position.z / u_cameraNearFar.y;\n}","fragment":"#version 120\n\nvarying float v_depth;\n\nvoid main() {\n     gl_FragColor \u003d vec4(vec3(1-v_depth), 1.0);\n}","primitive":"GL_TRIANGLES","canCompile":true}