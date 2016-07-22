package net.ncguy.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import net.ncguy.argent.Argent;
import net.ncguy.argent.editor.EditorModule;
import net.ncguy.argent.editor.EditorRoot;
import net.ncguy.argent.entity.EntityModule;
import net.ncguy.argent.entity.WorldEntity;
import net.ncguy.argent.entity.components.NameComponent;
import net.ncguy.argent.entity.components.RenderableComponent;
import net.ncguy.argent.render.AbstractWorldRenderer;
import net.ncguy.argent.render.BasicRenderModule;
import net.ncguy.argent.render.BasicWorldRenderer;
import net.ncguy.argent.utils.ScreenshotUtils;
import net.ncguy.argent.world.GameWorld;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.*;

/**
 * Created by Guy on 15/07/2016.
 */
public class GameScreen implements Screen {

    EditorRoot<WorldEntity> editorRoot;
    Stage stage;
    GameWorld<WorldEntity> world;
    AbstractWorldRenderer<WorldEntity> renderer;
    FrameBuffer fbo;

    @Override
    public void show() {
//        UIModule.handle(Gdx.files.internal("ui/uiskin.json"));
        Argent.loadModule(new EditorModule());
        Argent.loadModule(new BasicRenderModule());
        Argent.loadModule(new EntityModule());

        stage = new Stage(new ScreenViewport(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));
        world = new GameWorld<WorldEntity>() {
            @Override
            public WorldEntity buildInstance() {
                return new WorldEntity();
            }
        };
        renderer = new BasicWorldRenderer<>(world);
        editorRoot = new EditorRoot<>(world, stage, renderer::camera);

        WorldEntity e = new WorldEntity();
        world.addInstance(e);

        Model model = new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Position | Normal | TextureCoordinates);
        ModelInstance inst = new ModelInstance(model);
        e.add(new RenderableComponent(inst));
        e.add(new NameComponent("test"));

        e.invalidate();

        editorRoot.wrappedView(() -> fbo().getColorBufferTexture());

        Gdx.input.setInputProcessor(stage);
    }

    public FrameBuffer fbo() {
        if(this.fbo == null)
            fbo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return fbo;
    }

    public void fbo(int w, int h) {
        if(this.fbo != null) {
            this.fbo.dispose();
            this.fbo = null;
        }
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if(editorRoot.renderMain()) {
            if(editorRoot.wrapMainRender()) {
                fbo().begin();
                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
                renderer.render(delta);
                if(Gdx.input.isKeyJustPressed(Input.Keys.F2))
                    ScreenshotUtils.saveScreenshot(fbo().getWidth(), fbo().getHeight(), "Main");
                fbo().end();
            }else{
                renderer.render(delta);
            }
        }

        stage.act(delta);
        stage.draw();

        if(Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                if(editorRoot.attached()) editorRoot.remove();
                else editorRoot.attach();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        this.renderer.resize(width, height);
        this.stage.getViewport().update(width, height, true);
        this.stage.getCamera().update(true);
        fbo(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
