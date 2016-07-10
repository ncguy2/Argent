package net.ncguy.argent.editor.swing.config.descriptors.builders;

import net.ncguy.argent.editor.ConfigurableAttribute;
import net.ncguy.argent.utils.SwingUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by Guy on 01/07/2016.
 */
public class SwingComponentBuilder extends AbstractComponentBuilder {

    private static SwingComponentBuilder instance;
    public static SwingComponentBuilder instance() {
        if (instance == null)
            instance = new SwingComponentBuilder();
        return instance;
    }

    private SwingComponentBuilder() {}


    @Override
    public <T> JCheckBox buildCheckBox(ConfigurableAttribute<T> attr) {
        JCheckBox checkBox = new JCheckBox();

        String name = getValue(String.class, attr.params().get("name"));

        checkBox.setText(name);
        checkBox.setSelected((Boolean) attr.get());
        checkBox.addActionListener(e -> attr.set((T) (checkBox.isSelected()+"")));

        return checkBox;
    }

    @Override
    public <T> JTextField buildTextField(ConfigurableAttribute<T> attr) {
        JTextField field = new JTextField();

        String text = attr.get().toString();
        System.out.println("Attr: "+text);
        field.setText(text);
        field.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                attr.set((T)field.getText());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                attr.set((T)field.getText());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                attr.set((T)field.getText());
            }
        });

        return field;
    }

    @Override
    public <T> JComboBox buildComboBox(ConfigurableAttribute<T> attr) {
        JComboBox box = new JComboBox();
        DefaultComboBoxModel model = (DefaultComboBoxModel) box.getModel();

        Object[] items = getValue(Object[].class, attr.params().get("items"));

        model.removeAllElements();
        for (Object item : items)
            model.addElement(item);

        model.setSelectedItem(attr.get());
        box.addActionListener(e -> attr.set((T)box.getSelectedItem()));

        return box;
    }

    @Override
    public <T> JSpinner buildNumberSelector(ConfigurableAttribute<T> attr) {

        Integer min = getValue(Integer.class, attr.params().get("min"));
        Integer max = getValue(Integer.class, attr.params().get("max"));
        Integer precision = getValue(Integer.class, attr.params().get("precision"));

        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setMinimum(min);
        model.setMaximum(max);

        System.out.printf("min: %s, max: %s\n", min, max);

        JSpinner spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
        editor.getFormat().setMinimumFractionDigits(precision);
        SwingUtils.getSpinnerFormatter(spinner).setCommitsOnValidEdit(true);


        spinner.setValue(attr.get());
        spinner.addChangeListener(e -> attr.set((T)spinner.getValue()));

        return spinner;
    }

    @Override
    public <T> Object buildSelectionList(ConfigurableAttribute<T> attr) {
        return new JLabel("Unsupported component");
    }
}
