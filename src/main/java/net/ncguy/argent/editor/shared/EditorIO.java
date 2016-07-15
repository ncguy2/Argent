package net.ncguy.argent.editor.shared;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import net.ncguy.argent.Argent;
import net.ncguy.argent.io.IWritable;
import net.ncguy.argent.physics.BulletEntity;
import net.ncguy.argent.render.shader.DynamicShader;
import net.ncguy.argent.world.GameWorld;
import net.ncguy.argent.world.WorldObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guy on 09/07/2016.
 */
public class EditorIO<T> {

    private GameWorld.Generic<T> world;
    private Stage stage;
    private FileChooser chooser;

    public EditorIO(Stage stage, GameWorld.Generic<T> world) {
        this.stage = stage;
        this.world = world;
        this.chooser = new FileChooser(FileChooser.Mode.SAVE);
        this.chooser.setMultiSelectionEnabled(false);
    }

    public void save(Runnable callback) {
        chooser.setMode(FileChooser.Mode.SAVE);
        chooser.setDirectory(new File(""));
        chooser.setListener(new FileChooserAdapter(){
            @Override
            public void selected(Array<FileHandle> files) {
                FileHandle first = files.first();
                if(first != null)
                    save(first.file(), callback);
            }
        });
        this.chooser.centerWindow();
        this.stage.addActor(this.chooser.fadeIn());
    }

    public void load(Runnable callback) {
        chooser.setMode(FileChooser.Mode.OPEN);
        chooser.setDirectory(new File(""));
        chooser.setListener(new FileChooserAdapter(){
            @Override
            public void selected(Array<FileHandle> files) {
                FileHandle first = files.first();
                if(first != null) try {
                    load(first.file(), callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        this.chooser.centerWindow();
        this.stage.addActor(this.chooser.fadeIn());
    }

    public void save(File file, Runnable callback) {
        if(file.isDirectory()) {
            System.out.println("Not a file");
            return;
        }
        Path path = file.toPath();
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GameWorldSaveState saveState = new GameWorldSaveState<>(world);
        saveState.packData();
        Argent.serial.serialize(saveState, (bin) -> {
            try {
                Files.write(path, bin.toString().getBytes(), StandardOpenOption.CREATE_NEW , StandardOpenOption.WRITE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if(callback != null) callback.run();
    }

    public void load(File file, Runnable callback) throws IOException {
        if(file.isDirectory()) {
            System.out.println("Not a file");
            return;
        }
        Path path = file.toPath();
        List<String> lines = Files.readAllLines(path);
        StringBuilder sb = new StringBuilder();
        lines.forEach(s -> sb.append(s).append("\n"));
        String s = sb.toString();
        world.clear();
        WorldObjectSaveState state = Argent.serial.deserialize(s, WorldObjectSaveState.class);
        state.world = (GameWorld.Generic<WorldObject>) world;
        state.unpackData();

        if(state.world instanceof GameWorld.Physics) {
            state.instances.forEach(i -> {
                GameWorld.Physics<BulletEntity<WorldObject>> physWorld = (GameWorld.Physics<BulletEntity<WorldObject>>)world;
                physWorld.addInstance(new BulletEntity<>(physWorld, i.transform(), i));
            });
        }else{
            state.instances.forEach(((GameWorld.Generic<WorldObject>)world)::addInstance);
        }

        world.renderer().clearRenderPipe();
        world.renderer().compileDynamicRenderPipe(state.renderers);
        if(callback != null) callback.run();
    }

    public static class WorldObjectSaveState extends GameWorldSaveState<WorldObject> {
        @Override
        public void unpackData() {
            this.instances.forEach(w -> w.gameWorld = world);
            super.unpackData();
        }
    }

    public static class GameWorldSaveState<T> implements IWritable {
        public List<T> instances;
        public List<DynamicShader.Info> renderers;
        public transient GameWorld.Generic<T> world;

        public GameWorldSaveState() {
            this.world = null;
            this.instances = new ArrayList<>();
            this.renderers = new ArrayList<>();
        }

        public GameWorldSaveState(GameWorld.Generic<T> world) {
            this.world = world;
            this.instances = this.world.instances();
            this.renderers = new ArrayList<>();
            this.world.renderer().dynamicPipe().forEach(p -> this.renderers.add(p.info()));
        }

        @Override
        public void packData() {
            this.instances.forEach(i -> {
                if(i instanceof IWritable) ((IWritable) i).packData();
            });
            this.renderers.forEach(i -> {
                if(i instanceof IWritable) ((IWritable) i).packData();
            });
        }

        @Override
        public void unpackData() {
            this.instances.forEach(i -> {
                if(i instanceof IWritable) ((IWritable) i).unpackData();
            });
            this.renderers.forEach(i -> {
                if(i instanceof IWritable) ((IWritable) i).unpackData();
            });
        }
    }

}
