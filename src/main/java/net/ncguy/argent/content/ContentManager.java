package net.ncguy.argent.content;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import net.ncguy.argent.editor.project.Registry;
import net.ncguy.argent.utils.FileUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Guy on 15/07/2016.
 */
public class ContentManager {

    protected Map<String, String> assetMap;
    protected AssetManager manager;
    protected Consumer<AssetManager> finishedCallback;
    protected Set<FileRoot> fileRoots;
    protected boolean hasLoaded = false;
    protected boolean hasFinished = false;
    protected boolean canUpdate = false;

    public ContentManager() {
        initLoader();
    }

    protected void initLoader() {
        if(hasLoaded) {
            System.out.println("Content manager already loaded");
            return;
        }
        manager = new AssetManager();
        assetMap = new HashMap<>();
        fileRoots = Collections.synchronizedSet(new LinkedHashSet<>());
        hasLoaded = true;

        addDirectoryRoot(Registry.TEXTURES_DIR, Texture.class, true, "png", "jpg");
    }

    public void addDirectoryRoot(String root, Class<?> assetType, String... extensionFilters) {
        addDirectoryRoot(new FileRoot(root, assetType, extensionFilters));
    }
    public void addDirectoryRoot(String root, Class<?> assetType, boolean absolute, String... extensionFilters) {
        addDirectoryRoot(new FileRoot(root, assetType, absolute, extensionFilters));
    }

    public void addDirectoryRoot(FileRoot fileRoot) {
        this.fileRoots.add(fileRoot);
    }

    public void setOnFinish(Consumer<AssetManager> callback) {
        this.finishedCallback = callback;
    }

    public void start() {
        new Thread(() -> {
            for(FileRoot root : fileRoots) {
                // {@link FILE}
//                List<File> files = FileUtils.getAllFilesInDirectory(new File(root.root));
//                files.forEach(file -> {
//                    if(root.isExtensionValid(file.getName())) {
//                        String name = file.getName();
//                        String key = root.assetType.getSimpleName()+"_"+name.substring(0, name.lastIndexOf('.'));
//                        key = key.toLowerCase();
//                        assetMap.put(key, file.getPath());
//                        manager.load(file.getPath(), root.assetType);
//                    }
//                });
                // {@link FILEHANDLE}
                FileHandle rootHandle;
                if(root.absolute) rootHandle = Gdx.files.absolute(root.root);
                else rootHandle = Gdx.files.internal(root.root);
                List<FileHandle> handles;
                handles = FileUtils.getAllHandlesInDirectory(rootHandle);
                handles.forEach(handle -> {
                    if(root.isExtensionValid(handle.extension())) {
                        String key = root.assetType.getSimpleName()+"_"+handle.nameWithoutExtension();
                        key = key.toLowerCase();
                        assetMap.put(key, handle.path());
                        manager.load(handle.path(), root.assetType);
                    }
                });
            }
            canUpdate = true;
        }).start();
    }

    public <T> T get(String ref, Class<T> cls) {
        if(!ref.toLowerCase().startsWith(cls.getSimpleName().toLowerCase()))
            ref = cls.getSimpleName()+"_"+ref;
        return (T)get(ref);
    }

    public <T> T get(String ref) {
        ref = ref.toLowerCase();
        if(!assetMap.containsKey(ref))
            return null;
        String val = assetMap.get(ref);
        return manager.get(val);
    }

    public String[] getAllRefs(Class<?> cls) {
        List<String> keys = assetMap.keySet().stream().filter(s -> s.startsWith(cls.getSimpleName().toLowerCase())).collect(Collectors.toList());
        return keys.toArray(new String[keys.size()]);
    }

    public float progress() {
        if(!canUpdate) return 0;
        if(hasFinished) return 1;
        return manager.getProgress();
    }

    public boolean update() {
        if(!canUpdate) return false;
        if(hasFinished) return true;
        boolean done = manager.update();
        if(done) {
            hasFinished = true;
            if(finishedCallback != null)
                finishedCallback.accept(manager);
        }
        return done;
    }

    public Map<String, String> assetMap() { return assetMap; }

    // TODO Optimise
    public String getRef(Object asset) {
        try {
            Class cls = asset.getClass();
            String[] allRefs = getAllRefs(cls);
            for (String ref : allRefs) {
                Object obj = get(ref, cls);
                if (obj.equals(asset)) return ref;
            }
        }catch (Exception ignored) {}
        return null;
    }

    public static class FileRoot {
        public String root;
        public Class<?> assetType;
        public String[] extensionFilters;
        public boolean absolute = false;

        public FileRoot(String root, Class<?> assetType, String... extensionFilters) {
            this.root = root;
            this.assetType = assetType;
            this.extensionFilters = extensionFilters;
        }

        public FileRoot(String root, Class<?> assetType, boolean absolute, String... extensionFilters) {
            this.root = root;
            this.assetType = assetType;
            this.absolute = absolute;
            this.extensionFilters = extensionFilters;
        }

        public boolean isExtensionValid(String ext) {
            for(String filter : extensionFilters)
                if(filter.equalsIgnoreCase(ext)) return true;
            return false;
        }
    }

}
