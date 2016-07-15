package net.ncguy.argent.data.tree;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Guy on 15/07/2016.
 */
public class VisitableTree<T> implements Visitable<T> {

    public final Set<VisitableTree<T>> children = new LinkedHashSet<>();
    public final T data;

    public VisitableTree(T data) {
        this.data = data;
    }

    @Override
    public void accept(Visitor<T> visitor) {
        visitor.visitData(this, data());
        for(VisitableTree<T> child : children)
            child.accept(visitor.visit(child));
    }

    public VisitableTree<T> child(T data) {
        for(VisitableTree<T> child : children) {
            if(child.data().equals(data)) return child;
        }
        return child(new VisitableTree<>(data));
    }

    public VisitableTree<T> child(VisitableTree<T> child) {
        children.add(child);
        return child;
    }

    @Override
    public T data() {
        return this.data;
    }
}