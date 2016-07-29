package net.ncguy.argent.editorOld.panels;

import net.ncguy.argent.editor.EditorRoot;
import net.ncguy.argent.entity.WorldEntity;

/**
 * Created by Guy on 17/07/2016.
 */
public abstract class AbstractEditorPanel<T extends WorldEntity> extends EditorRoot.PackedTab {

    EditorRoot<T> editorRoot;

    public AbstractEditorPanel(EditorRoot<T> editorRoot, String name) {
        this(editorRoot, name, false, false);
    }

    public AbstractEditorPanel(EditorRoot<T> editorRoot, String name, boolean savable) {
        this(editorRoot, name, savable, false);
    }

    public AbstractEditorPanel(EditorRoot<T> editorRoot, String name, boolean savable, boolean closeableByUser) {
        super(name, savable, closeableByUser);
        this.editorRoot = editorRoot;
        init();
    }

    protected abstract void init();

}
