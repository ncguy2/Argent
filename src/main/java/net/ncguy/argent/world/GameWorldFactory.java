package net.ncguy.argent.world;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import net.ncguy.argent.Argent;
import net.ncguy.argent.physics.DefaultMotionState;
import net.ncguy.argent.render.WorldRenderer;
import net.ncguy.argent.world.components.RenderingComponent;
import net.ncguy.argent.world.components.TransformComponent;

import java.util.List;
import java.util.Optional;

/**
 * Created by Guy on 21/06/2016.
 */
public class GameWorldFactory {

    public static GameWorld.Generic<WorldObject> worldObjectWorld(List<WorldObject> instances) {
        WorldRenderer<WorldObject> renderer = new WorldRenderer<WorldObject>(instances) {
            @Override
            public ModelInstance getRenderable(WorldObject obj) {
                return obj.instance;
            }

            @Override
            public void buildBulletCollision(WorldObject obj, btCollisionShape shape) {
            }
        };
        return new GameWorld.Generic<>(renderer, instances);
    }

    public static GameWorld.Physics<WorldObject> worldObjectPhysics(List<WorldObject> instances) {
        WorldRenderer<WorldObject> renderer = new WorldRenderer<WorldObject>(instances) {
            @Override
            public ModelInstance getRenderable(WorldObject obj) {
                return obj.instance;
            }

            @Override
            public void buildBulletCollision(WorldObject obj, btCollisionShape shape) {
                DefaultMotionState motionState = new DefaultMotionState();
                motionState.transform = obj.transform;
                Vector3 inertia = new Vector3();
                if (obj.mass > 0)
                    shape.calculateLocalInertia(obj.mass, inertia);
                btRigidBody.btRigidBodyConstructionInfo info = new btRigidBody.btRigidBodyConstructionInfo(obj.mass, motionState, shape, inertia);
                obj.body = new btRigidBody(info);
                obj.body.userData = obj;
                obj.shape = shape;
                info.dispose();

                switch(obj.physicsState()) {
                    case STATIC:    obj.setStatic();    break;
                    case KINEMATIC: obj.setKinematic(); break;
                    case DYNAMIC:   obj.setDynamic();   break;
                }
            }

            @Override
            public btRigidBody getBulletBody(WorldObject obj) {
                if(obj.body == null) {
                    Optional<ModelInstance> inst = getRenderableOptional(obj);
                    if (inst.isPresent())
                        Argent.physics.buildComplexCollisionMesh(this, obj);
                }
                return obj.body;
            }
        };
        return new GameWorld.Physics<>(renderer, instances);
    }

    public static class ComponentMappers {

        public static final ComponentMapper<RenderingComponent> renderingComponent = ComponentMapper.getFor(RenderingComponent.class);
        public static final ComponentMapper<TransformComponent> transformComponent = ComponentMapper.getFor(TransformComponent.class);

    }

}
