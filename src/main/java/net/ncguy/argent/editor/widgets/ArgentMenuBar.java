package net.ncguy.argent.editor.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.MenuBar;
import net.ncguy.argent.editor.EditorUI;
import net.ncguy.argent.editor.widgets.menubar.EditMenu;
import net.ncguy.argent.editor.widgets.menubar.FileMenu;
import net.ncguy.argent.editor.widgets.menubar.SettingsMenu;
import net.ncguy.argent.editor.widgets.menubar.ToolMenu;

/**
 * Created by Guy on 27/07/2016.
 */
public class ArgentMenuBar extends MenuBar {

    private FileMenu fileMenu;
    private EditMenu editMenu;
    private ToolMenu toolMenu;
    private SettingsMenu settingsMenu;

    private EditorUI editorUI;

    public ArgentMenuBar(EditorUI editorUI) {
        super();
        this.editorUI = editorUI;
        fileMenu = new FileMenu();
        editMenu = new EditMenu(editorUI);
        toolMenu = new ToolMenu(editorUI);
        settingsMenu = new SettingsMenu(editorUI);

        addMenu(fileMenu);
        addMenu(editMenu);
        addMenu(toolMenu);
        addMenu(settingsMenu);
    }

    @Override
    public Table getTable() {
//        Table root = new Table(VisUI.getSkin());
//        root.setBackground("menu-bg");
//        Table menuTable = super.getTable();

        return super.getTable();
    }
}
