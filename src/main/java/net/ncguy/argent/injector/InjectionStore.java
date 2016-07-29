package net.ncguy.argent.injector;

import net.ncguy.argent.Argent;
import net.ncguy.argent.editor.CommandHistory;
import net.ncguy.argent.editor.EditorUI;
import net.ncguy.argent.editor.project.ProjectManager;
import net.ncguy.argent.entity.ComponentSet;
import net.ncguy.argent.misc.FreeCamController;

import java.lang.reflect.Field;

/**
 * Created by Guy on 27/07/2016.
 */
public class InjectionStore {

    public static void setGlobal(Object obj) throws IllegalAccessException {
        Field targetField = findTargetField(obj.getClass());
        if(targetField != null) {
            targetField.setAccessible(true);
            targetField.set(null, obj);
        }else{
            Argent.injector.log("Error setting global of type %s to %s", obj.getClass().getSimpleName(), obj.toString());
        }
    }

    private static Field findTargetField(Class cls) {
        for (Field field : InjectionStore.class.getDeclaredFields()) {
            if(field.getType().equals(cls)) return field;
        }
        return null;
    }

    private static FreeCamController freeCamController;
    private static ProjectManager projectManager;
    private static CommandHistory commandHistory;
    private static EditorUI editorUI;
    private static ComponentSet availableComponents;


}
