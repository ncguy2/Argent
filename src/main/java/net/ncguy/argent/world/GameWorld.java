package net.ncguy.argent.world;

import com.badlogic.gdx.utils.Disposable;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guy on 15/07/2016.
 */
public class GameWorld<T> implements Disposable {

    protected List<T> instances;

    public GameWorld() {
        this(new ArrayList<>());
    }

    public GameWorld(@NotNull List<T> instances) {
        this.instances = instances;
    }

    public List<T> instances() { return instances;}

    public void addInstance(T instance) {
        if(this.containsInstance(instance))
            this.removeInstance(instance);
        this.instances.add(instance);
    }

    public void removeInstance(T instance) {
        this.instances.remove(instance);
    }

    public boolean containsInstance(T instance) {
        return this.instances.contains(instance);
    }

    @Override
    public void dispose() {
        this.instances.forEach(i -> {
            if(i instanceof Disposable) ((Disposable)i).dispose();
        });
    }
}
