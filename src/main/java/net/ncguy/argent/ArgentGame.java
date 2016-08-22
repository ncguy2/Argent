package net.ncguy.argent;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

/**
 * Created by Guy on 15/07/2016.
 */
public abstract class ArgentGame extends Game {

    @Override
    public void create() {}

    @Override
    public void setScreen(Screen screen) {
        Argent.loadModule(new CoreModule());
        super.setScreen(screen);
    }

    @Override
    public void render() {
        super.render();
        if(Argent.tween != null)
            Argent.tween.update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        Argent.onResize(width, height);
    }


}
