package net.ncguy.argent.render.argent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.ncguy.argent.Argent;
import net.ncguy.argent.editor.widgets.DebugPreview;
import net.ncguy.argent.entity.WorldEntity;
import net.ncguy.argent.entity.components.ArgentComponent;
import net.ncguy.argent.entity.components.LightComponent;
import net.ncguy.argent.event.StringPacketEvent;
import net.ncguy.argent.render.BasicWorldRenderer;
import net.ncguy.argent.utils.AppUtils;
import net.ncguy.argent.utils.MultiTargetFrameBuffer;
import net.ncguy.argent.utils.ScreenshotUtils;
import net.ncguy.argent.world.GameWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guy on 23/07/2016.
 */
public class ArgentRenderer<T extends WorldEntity> extends BasicWorldRenderer<T> {

    MultiTargetFrameBuffer textureMRT;
    ModelBatch textureBatch;
    ShaderProgram textureProgram;

    MultiTargetFrameBuffer lightingMRT;
    ModelBatch lightingBatch;
    ShaderProgram lightingProgram;

//    SpriteBatch screenBatch;
    ShaderProgram screenProgram;

    Vector2 size = new Vector2();
//    Sprite screenSprite;

    public static final FBOAttachment tex_NORMAL = new FBOAttachment(0, "texNormal");
    public static final FBOAttachment tex_DIFFUSE = new FBOAttachment(1, "texDiffuse");
    public static final FBOAttachment tex_SPCAMBDIS = new FBOAttachment(2, "texSpcAmbDis");
//    public static final FBOAttachment tex_SPECULAR = new FBOAttachment(2, "texSpecular");
//    public static final FBOAttachment tex_AMBIENT = new FBOAttachment(3, "texAmbient");
//    public static final FBOAttachment tex_DISPLACEMENT = new FBOAttachment(4, "texDisplacement");
    public static final FBOAttachment tex_EMISSIVE = new FBOAttachment(3, "texEmissive");
    public static final FBOAttachment tex_REFLECTION = new FBOAttachment(4, "texReflection");
    public static final FBOAttachment tex_POSITION = new FBOAttachment(5, "texPosition");
    public static final FBOAttachment tex_MODIFIEDNORMAL = new FBOAttachment(6, "texModNormal");

    public static final FBOAttachment[] tex_ATTACHMENTS = new FBOAttachment[]{
        tex_NORMAL, tex_DIFFUSE, tex_SPCAMBDIS, tex_EMISSIVE, tex_REFLECTION, tex_POSITION, tex_MODIFIEDNORMAL
    };

    public static final FBOAttachment ltg_POSITION = new FBOAttachment(0, "ltgPosition");
//    public static final FBOAttachment ltg_DEPTH = new FBOAttachment(0, "ltgPosition");
    public static final FBOAttachment ltg_TEXTURES = new FBOAttachment(1, "ltgTextures");
    public static final FBOAttachment ltg_LIGHTING = new FBOAttachment(2, "ltgLighting");
    public static final FBOAttachment ltg_GEOMETRY = new FBOAttachment(3, "ltgGeometry");


    public static final FBOAttachment[] ltg_ATTACHMENTS = new FBOAttachment[] {
        ltg_POSITION, ltg_TEXTURES, ltg_LIGHTING, ltg_GEOMETRY
    };

    public ArgentRenderer(GameWorld<T> world) {
        super(world);
        size.set(camera().viewportWidth, camera().viewportHeight);
        refreshShaders();
        refreshFBO();
        Argent.addOnResize(this::resize);
        Argent.addOnResize(this::argentResize);
        Argent.addOnKeyDown(key -> {
            if(key == Input.Keys.O)
                if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
                    refreshShaders();
        });
    }

    @Override
    public ModelBatch batch() {
        if(modelBatch == null) {
            screenProgram = AppUtils.Shader.loadGeometryShader("pipeline/screen");
            modelBatch = new ModelBatch(new DefaultShaderProvider() {
                @Override
                protected Shader createShader(Renderable renderable) {
                    return new ArgentScreenShader(renderable, screenProgram);
                }
            });
        }
        return modelBatch;
    }

    public void argentResize(int width, int height) {
        size.set(width, height);
        camera().viewportWidth = width;
        camera().viewportHeight = height;
        camera().update(true);
        if(width <= 0 || height <= 0) return;
        refreshFBO();
    }

    @Override
    public void resize(int width, int height) {
        if(width == 0 || height == 0) return;
        if(camera().viewportWidth == width && camera().viewportHeight == height) return;
        super.resize(width, height);
//        System.out.printf("[%s, %s]\n", width, height);
//        refreshShaders();
    }

    @Override
    public void render(ModelBatch batch, float delta) {
        renderTexture(delta);
        applyGBufferToLighting();
        renderLighting(delta);
        applyLightingToQuad();
        renderToScreen(delta);
    }

    private void renderToMRT(MultiTargetFrameBuffer mrt, ModelBatch batch, float delta) {
        mrt.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        super.render(batch, delta);
        if(Gdx.input.isKeyJustPressed(Input.Keys.F2))
            ScreenshotUtils.saveScreenshot(iWidth(), iHeight(), "Batch");
        mrt.end();
    }

    public void renderTexture(float delta) {
        renderToMRT(textureMRT, textureBatch, delta);
    }
    public void renderLighting(float delta) {
        renderToMRT(lightingMRT, lightingBatch, delta);
    }
    public void renderToScreen(float delta) {
        super.render(batch(), delta);
    }

    public void applyGBufferToLighting() {
        lightingProgram.begin();
        bindToMRT(textureMRT, lightingProgram, tex_ATTACHMENTS);
        bindLightingData(lightingProgram);
        lightingProgram.end();
    }
    public void bindLightingData(ShaderProgram shaderProgram) {
        final List<LightComponent> lights = new ArrayList<>();
        world.instances().forEach(i -> i.components().stream()
            .filter(this::bindLightData_Filter)
            .map(this::bindLightData_Map)
            .forEach(lights::add));
        final int[] i = new int[]{0};
        lights.forEach(l -> {
            String key = "lights["+i[0]+"]";
            Vector3 worldPos = l.getWorldPosition();
            float[] pos = new float[]{
                    worldPos.x,
                    worldPos.y,
                    worldPos.z
            };
            Color colour = l.getColour();
            float[] col = new float[]{
                    colour.r,
                    colour.g,
                    colour.b
            };
            shaderProgram.setUniform3fv(key+".Position", pos, 0, pos.length);
            shaderProgram.setUniform3fv(key+".Colour", col, 0, col.length);
            shaderProgram.setUniformf(key+".Linear", l.getLinear());
            shaderProgram.setUniformf(key+".Quadratic", l.getQuadratic());
            shaderProgram.setUniformf(key+".Intensity", l.getIntensity());
            shaderProgram.setUniformf(key+".Radius", l.getRadius());
            i[0]++;
        });
    }
    private boolean bindLightData_Filter(ArgentComponent c) {
        return c instanceof LightComponent;
    }
    private LightComponent bindLightData_Map(ArgentComponent c) {
        return (LightComponent)c;
    }

    public void applyLightingToQuad() {
        screenProgram.begin();
        bindToMRT(lightingMRT, screenProgram, 7, ltg_ATTACHMENTS);
        screenProgram.end();
    }

    private void bindToMRT(MultiTargetFrameBuffer mrt, ShaderProgram shader, FBOAttachment... attachments) {
        bindToMRT(mrt, shader, 0, attachments);
    }

    private void bindToMRT(MultiTargetFrameBuffer mrt, ShaderProgram shader, int offset, FBOAttachment... attachments) {
        for (FBOAttachment attachment : attachments) {
            int id = attachment.id + offset;
            Texture tex = mrt.getColorBufferTexture(attachment.id);
            if(tex != null) tex.bind(id);
            attachment.bind(shader, id);
        }
        mrt.getColorBufferTexture(tex_DIFFUSE.id).bind(tex_DIFFUSE.id);
        shader.setUniformi(tex_DIFFUSE.name, tex_DIFFUSE.id);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    }

    public MultiTargetFrameBuffer getTextureMRT()  { return textureMRT;  }
    public MultiTargetFrameBuffer getLightingMRT() { return lightingMRT; }

    public void refreshFBO() {
        if(textureMRT != null) {
            textureMRT.dispose();
            textureMRT = null;
        }
        if(lightingMRT != null) {
            lightingMRT.dispose();
            lightingMRT = null;
        }

        textureMRT = create(tex_ATTACHMENTS.length);
        lightingMRT = create(ltg_ATTACHMENTS.length);

        DebugPreview debugPreview = DebugPreview.instance();
        if(debugPreview != null) debugPreview.build(textureMRT, lightingMRT);
    }

    public void refreshShaders() {
        new StringPacketEvent("toast|info", "Reloading Shaders").fire();
        ShaderProgram.pedantic = false;
        refreshTextureShader();
        refreshLightingShader();
        refreshScreenShader();
    }

    public void refreshTextureShader() {
        if(textureProgram != null) {
            textureProgram.dispose();
            textureProgram = null;
        }
        if(textureBatch != null) {
            textureBatch.dispose();
            textureBatch = null;
        }
        textureProgram = AppUtils.Shader.loadGeometryShader("pipeline/texture");
        textureBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(Renderable renderable) {
                return new SmartTextureShader(renderable, textureProgram);
            }
        });
    }
    public void refreshLightingShader() {
        if(lightingProgram != null) {
            lightingProgram.dispose();
            lightingProgram = null;
        }
        if(lightingBatch != null) {
            lightingBatch.dispose();
            lightingBatch = null;
        }
        lightingProgram = AppUtils.Shader.loadGeometryShader("pipeline/lighting");
        lightingBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(Renderable renderable) {
                return new ArgentShader(renderable, lightingProgram);
            }
        });
    }
    public void refreshScreenShader() {
        if(screenProgram != null) {
            screenProgram.dispose();
            screenProgram = null;
        }
        if(modelBatch != null) {
            modelBatch.dispose();
            modelBatch = null;
        }
        batch();
    }

    private MultiTargetFrameBuffer create(int bufferCount) {
        return MultiTargetFrameBuffer.create(MultiTargetFrameBuffer.Format.RGBA32F, bufferCount,
                iWidth(), iHeight(), true, false);
    }

    public float width() {
        return size.x;
    }
    public float height() {
        return size.y;
    }

    public int iWidth() {
        return (int) width();
    }
    public int iHeight() {
        return (int) height();
    }

    public static class FBOAttachment {
        public int id;
        public String name;

        public FBOAttachment(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public void bind(ShaderProgram program) {
            bind(program, this.id);
        }
        public void bind(ShaderProgram program, int id) {
            program.setUniformi(this.name, id);
        }
    }

}
