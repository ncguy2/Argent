{"name":"NewShader","desc":"Looks make fancy","vertex":"#version 330\n\nattribute vec4 a_position;\n\nuniform mat4 u_projViewTrans;\nuniform mat4 u_worldTrans;\n\nvoid main() {\n\tgl_Position \u003d u_projViewTrans * (u_worldTrans * a_position);\n}\n","fragment":"#version 330\n\nuniform vec2 u_viewportSize;\nuniform sampler2D u_SimpleDepth;\nuniform sampler2D u_DiffuseShader;\nuniform sampler2D u_Wireframe;\nuniform sampler2D u_UberShader;\nuniform sampler2D u_UberDepthShader;\n\nvoid main() {\n\tvec2 texel \u003d gl_FragCoord.xy / u_viewportSize;\n\n\tvec4 depth \u003d texture2D(u_SimpleDepth, texel);\n\tvec4 diffuse \u003d texture2D(u_DiffuseShader, texel);\n\tvec4 wireframe \u003d texture2D(u_Wireframe, texel);\n\tvec4 uber \u003d texture2D(u_UberShader, texel);\n\tvec4 uberDepth \u003d texture2D(u_UberDepthShader, texel);\n\n\tvec4 col \u003d uber;\n\tcol *\u003d diffuse*5;\n\tcol.rgb +\u003d wireframe.rgb;\n\n\tdepth.r *\u003d uberDepth.a;\n\n\tvec4 finalColour \u003d vec4(col.rgb*(depth.r), col.a);\n\n//\tfinalColour *\u003d 12;\n\n\tgl_FragColor \u003d finalColour;\n}\n","primitive":"GL_TRIANGLES","canCompile":true}