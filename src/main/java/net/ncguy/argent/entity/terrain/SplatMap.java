package net.ncguy.argent.entity.terrain;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by Guy on 27/07/2016.
 */
public class SplatMap implements Disposable {

    public static final int DEFAULT_SIZE = 512;

    private int width, height;

    private Pixmap pixmap;
    private Texture texture;
    private String path;

    private final Color c0 = new Color();

    public SplatMap(int width, int height) {
        Pixmap.setBlending(Pixmap.Blending.None);
        this.pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        this.texture = new Texture(this.pixmap);
        clear();

        this.width = width;
        this.height = height;
    }

    public SplatMap(Pixmap pixmap) {
        Pixmap.setBlending(Pixmap.Blending.None);

        this.pixmap = pixmap;
        texture = new Texture(pixmap);

        width = pixmap.getWidth();
        height = pixmap.getHeight();
    }

    public Texture getTexture() {
        return texture;
    }

    public Pixmap getPixmap() {
        return pixmap;
    }

    public void clearChannel(SplatTexture.Channel channel) {
        for(int smX = 0; smX < pixmap.getWidth(); smX++) {
            for(int smY = 0; smY < pixmap.getHeight(); smY++) {
                c0.set(pixmap.getPixel(smX, smY));
                if(channel == SplatTexture.Channel.R) {
                    c0.set(0, c0.g, c0.b, c0.a);
                } else if(channel == SplatTexture.Channel.G) {
                    c0.set(c0.r, 0, c0.b, c0.a);
                } else if(channel == SplatTexture.Channel.B) {
                    c0.set(c0.r, c0.g, 0, c0.a);
                } else if(channel == SplatTexture.Channel.A) {
                    c0.set(c0.r, c0.g, c0.b, 0);
                }
                pixmap.drawPixel(smX, smY, Color.rgba8888(c0));
            }
        }
    }

    public void clear() {
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fillRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());
        updateTexture();
    }

    public void updateTexture() {
        texture.draw(pixmap, 0, 0);
    }

    public void savePNG(FileHandle fileHandle) {
        PixmapIO.writePNG(fileHandle, pixmap);
    }

    public void loadPNG(FileHandle fileHandle) {
        pixmap = new Pixmap(fileHandle);
        updateTexture();

        this.width = pixmap.getWidth();
        this.height = pixmap.getHeight();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int additiveBlend(int pixelColor, SplatTexture.Channel channel, float strength) {
        c0.set(pixelColor);
        if(channel == SplatTexture.Channel.BASE) {
            c0.sub(strength, strength, strength, strength);
        } else if(channel == SplatTexture.Channel.R) {
            c0.add(strength, 0, 0, 0);
        } else if(channel == SplatTexture.Channel.G) {
            c0.add(0, strength, 0, 0);
        } else if(channel == SplatTexture.Channel.B) {
            c0.add(0, 0, strength, 0);
        } else if(channel == SplatTexture.Channel.A) {
            c0.add(0, 0, 0, strength);
        }

        // prevent the sum to be greater than 1
        final float sum = c0.r + c0.g + c0.b + c0.a;
        if(sum > 1f) {
            final float correction = 1f / sum;
            c0.r *= correction;
            c0.g *= correction;
            c0.b *= correction;
            c0.a *= correction;
        }

        return Color.rgba8888(c0);
    }

    @Override
    public void dispose() {
        pixmap.dispose();
    }
}
