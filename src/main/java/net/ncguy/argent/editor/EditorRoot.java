package net.ncguy.argent.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneAdapter;
import net.ncguy.argent.Argent;
import net.ncguy.argent.editorOld.panels.ObjectDataPanel;
import net.ncguy.argent.entity.WorldEntity;
import net.ncguy.argent.entity.components.factory.ArgentComponentFactory;
import net.ncguy.argent.world.GameWorld;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by Guy on 17/07/2016.
 */
public class EditorRoot<T extends WorldEntity> {

    protected TabbedPane tabbedPane;
    protected Stage stage;
    protected Group activePane;
    protected boolean attached;
    protected PackedTab selectedTab;
    protected GameWorld<T> world;
    protected T selectedObject;
    protected Vector3 camDirection;
    protected Vector3 camPosition;
    protected Vector3 camUp;
    protected Vector2 camSize;
    protected Supplier<Camera> cameraSupplier;
    protected Supplier<Texture> wrappedView;
    protected InputListener stageFocusManager;
    protected Actor dummyActor;
    protected boolean cameraCached = false;

    private static List<ArgentComponentFactory> componentFactoryList;
    public static List<ArgentComponentFactory> componentFactoryList() {
        if(componentFactoryList == null) {
            componentFactoryList = new ArrayList<>();
            String pkg = ArgentComponentFactory.class.getPackage().getName();
            Set<Class<? extends ArgentComponentFactory>> clsSet = new Reflections(pkg).getSubTypesOf(ArgentComponentFactory.class);
            clsSet.forEach(cls -> {
                try {
                    ArgentComponentFactory factory = cls.getConstructor().newInstance();
                    componentFactoryList.add(factory);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        }
        return componentFactoryList;
    }

    // Panel cache
    public ObjectDataPanel objDataPanel;

    public EditorRoot(GameWorld<T> world, Stage stage, Supplier<Camera> cameraSupplier) {
        this.world = world;
        this.stage = stage;
        this.cameraSupplier = cameraSupplier;
        this.camDirection = new Vector3();
        this.camPosition = new Vector3();
        this.camUp = new Vector3();
        this.camSize = new Vector2();
        this.dummyActor = new Actor() {
            @Override
            public void act(float delta) {
                EditorRoot.this.act(delta);
            }

            @Override
            public void draw(Batch batch, float parentAlpha) {
                EditorRoot.this.draw(batch, parentAlpha);
            }
        };
        stage.addActor(this.dummyActor);
        this.stageFocusManager = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.ESCAPE)
                    stageFocus();
                return super.keyDown(event, keycode);
            }
        };
    }

    public void stageFocus() {
        this.stage.unfocusAll();
    }

    public T selected() { return selectedObject; }
    public void select(T obj) { selectedObject = obj; }

    public Supplier<Texture> wrappedView() { return wrappedView; }
    public void wrappedView(Supplier<Texture> wrappedView) { this.wrappedView = wrappedView; }

    public Texture getWrappedView() {
        if(!wrapMainRender()) return null;
        if(this.wrappedView == null) return null;
        return this.wrappedView.get();
    }

    public boolean renderMain() {
        return this.selectedTab == null || !this.selectedTab.blockMainRender();
    }
    public boolean wrapMainRender() {
        if(this.selectedTab == null) return false;
        if(!this.selectedTab.blockMainRender()) {
            return this.selectedTab.wrapMainRender();
        }
        return false;
    }
    public boolean attached() { return this.attached; }

    public Supplier<Camera> cameraSupplier() { return cameraSupplier; }

    public void cacheCamera() {
        if(this.cameraCached) return;
        Camera camera = this.cameraSupplier.get();
        this.camDirection.set(camera.direction);
        this.camPosition.set(camera.position);
        this.camUp.set(camera.up);
        this.camSize.set(camera.viewportWidth, camera.viewportHeight);
        this.cameraCached = true;
    }

    public void positionCamera(Vector3 pos) {
        Camera camera = this.cameraSupplier.get();
        camera.position.set(pos);
        camera.update(true);
    }
    public void directCamera(Vector3 dir) {
        Camera camera = this.cameraSupplier.get();
        camera.direction.set(dir);
        camera.update(true);
    }
    public void setCamera(Vector3 pos, Vector3 dir) {
        positionCamera(pos);
        directCamera(dir);
    }

    public void resizeCamera() { resizeCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); }
    public void resizeCamera(float w, float h) {
        Camera camera = this.cameraSupplier.get();
        camera.viewportWidth = w;
        camera.viewportHeight = h;
        camera.update(true);
    }

    public void revertCamera() {
        if(!this.cameraCached) return;
        Camera camera = this.cameraSupplier.get();
        camera.direction.set(this.camDirection);
        camera.position.set(this.camPosition);
        camera.up.set(this.camUp);
        camera.viewportWidth = this.camSize.x;
        camera.viewportHeight = this.camSize.y;
        camera.update(true);
        this.cameraCached = false;
    }

    public Stage stage() { return stage; }

    public void act(float delta) {
        if(this.selectedTab != null) this.selectedTab.act(delta);
    }

    public void draw(Batch batch, float parentAlpha) {
        if(this.selectedTab != null) this.selectedTab.draw(batch, parentAlpha);
    }

    public void attach() {
        this.tabbedPane = new TabbedPane();
        this.activePane = new Group();

        this.tabbedPane.addListener(new TabbedPaneAdapter() {
            @Override
            public void switchedTab(Tab tab) {
                if(selectedTab != null)
                    selectedTab.onSwitchFrom();
                activePane.clear();
                selectedTab = null;
                if(tab instanceof PackedTab) {
                    PackedTab pack = (PackedTab)tab;
                    activePane.addActor(pack.getGroup());
                    selectedTab = pack;
                    selectedTab.onSwitchTo();
                }
                resizeTab((int)activePane.getWidth(), (int)activePane.getHeight());
            }
        });
        Tab tab;
        this.tabbedPane.add(tab = new EmptyTab(false, false, "Main"));
        this.tabbedPane.add(objDataPanel = new ObjectDataPanel<>(this));

        this.tabbedPane.switchTab(tab);

        Argent.addOnResize(this::resize);
        this.stage.addActor(this.tabbedPane.getTable());
        this.stage.addActor(this.activePane);
        this.attached = true;
        this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.tabbedPane.getTable().setY(Gdx.graphics.getHeight());
        this.tabbedPane.getTable().addAction(Actions.moveTo(0, Gdx.graphics.getHeight()-this.tabbedPane.getTable().getHeight(), .3f));
        this.stage.addListener(stageFocusManager);
    }

    public void remove() {
        if(this.attached) {
            this.tabbedPane.getTable().remove();
            this.activePane.remove();
            Argent.removeOnResize(this::resize);
            this.stage.removeListener(stageFocusManager);
            this.attached = false;
            this.objDataPanel = null;
        }
    }

    public void resize(int w, int h) {
        if(!this.attached) return;

        this.tabbedPane.getTable().pack();
        this.tabbedPane.getTable().setSize(w, 26);
        this.tabbedPane.getTable().setPosition(0, h-this.tabbedPane.getTable().getHeight());

        h = (int) this.tabbedPane.getTable().getY();
        this.activePane.setBounds(0, 0, w, h);
        resizeTab(w, h);
    }

    public void resizeTab(int w, int h) {
        if(this.selectedTab != null) this.selectedTab.resize(w, h);
    }

    public boolean cached() {
        return this.cameraCached;
    }

    public GameWorld<T> world() { return world; }

    public static class PackedTab extends Tab {

        protected Group group;
        protected String name;

        public PackedTab(String name) {
            this.name = name;
        }

        public PackedTab(String name, boolean savable) {
            super(savable);
            this.name = name;
        }

        public PackedTab(String name, boolean savable, boolean closeableByUser) {
            super(savable, closeableByUser);
            this.name = name;
        }

        public PackedTab setGroup(Group grp) {
            this.group = grp;
            return this;
        }
        public Group getGroup() { return this.group; }

        @Override
        public String getTabTitle() {
            return this.name;
        }

        @Override
        public Table getContentTable() {
            return null;
        }

        public boolean blockMainRender() {
            return true;
        }

        public boolean wrapMainRender() {
            return false;
        }


        /**
         * Executes if the tab is selected
         * @param delta
         */
        public void act(float delta) {}
        public void draw(Batch batch, float parentAlpha) {}
        public void onSwitchTo() {}
        public void onSwitchFrom() {}
        public void resize(int w, int h) {}
    }

    public static class EmptyTab extends Tab {
        private String name;

        public EmptyTab(String name) {
            this.name = name;
        }

        public EmptyTab(boolean savable, String name) {
            super(savable);
            this.name = name;
        }

        public EmptyTab(boolean savable, boolean closeableByUser, String name) {
            super(savable, closeableByUser);
            this.name = name;
        }

        @Override
        public String getTabTitle() {
            return this.name;
        }

        @Override
        public Table getContentTable() {
            return null;
        }
    }

}
