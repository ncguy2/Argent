package net.ncguy.argent.editor.swing.config.descriptors;

import net.ncguy.argent.core.BasicEntry;
import net.ncguy.argent.editor.swing.config.ConfigControl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Guy on 01/07/2016.
 */
public class TextFieldDescriptor extends ControlConfigDescriptor {

    @Override
    public Map<String, BasicEntry<Class<?>, Object>> attributes() {
        Map<String, BasicEntry<Class<?>, Object>> map = new HashMap<>();
        add(map, "text", String.class, "");
        return map;
    }
}
