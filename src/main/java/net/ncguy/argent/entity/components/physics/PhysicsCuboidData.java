package net.ncguy.argent.entity.components.physics;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Created by Guy on 22/09/2016.
 */
public class PhysicsCuboidData extends PhysicsData {

    public float halfWidth, halfHeight, halfDepth;

    public PhysicsCuboidData(PhysicsComponent parentComponent, float halfWidth, float halfHeight, float halfDepth) {
        super(parentComponent);
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.halfDepth = halfDepth;
    }

    public PhysicsCuboidData(PhysicsComponent parentComponent) {
        this(parentComponent, .5f, .5f, .5f);
    }

    public float getHalfWidth()  { return halfWidth;  }
    public float getHalfHeight() { return halfHeight; }
    public float getHalfDepth()  { return halfDepth;  }

    public void setHalfWidth(float halfWidth)   { this.halfWidth  = Math.abs(halfWidth);  }
    public void setHalfHeight(float halfHeight) { this.halfHeight = Math.abs(halfHeight); }
    public void setHalfDepth(float halfDepth)   { this.halfDepth  = Math.abs(halfDepth);  }

    @Override
    public String name() {
        return "Cuboid";
    }

    @Override
    public void generateBoundingBox(BoundingBox box) {
        halfWidth = Math.abs(halfWidth);
        halfHeight = Math.abs(halfHeight);
        halfDepth = Math.abs(halfDepth);

        Vector3 min = new Vector3(-halfWidth, -halfHeight, -halfDepth);
        Vector3 max = new Vector3(halfWidth, halfHeight, halfDepth);

        min.add(parentComponent.getWorldEntity().localPosition);
        max.add(parentComponent.getWorldEntity().localPosition);

        box.set(min, max);
    }
}
