package net.ncguy.argent.event;

/**
 * Created by Guy on 29/07/2016.
 */
public class SceneGraphChangedEvent {

    public static interface SceneGraphChangedListener {
        @Subscribe
        public void onSceneGraphChanged(SceneGraphChangedEvent sceneGraphChangedEvent);
    }

}