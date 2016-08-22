package net.ncguy.argent.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLOnlyTextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.badlogic.gdx.Gdx.gl30;
import static com.badlogic.gdx.graphics.GL30.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_BORDER_COLOR;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;

/**
 * Created by Guy on 23/07/2016.
 */
public class MultiTargetFrameBuffer extends GLFrameBuffer<Texture> {

    protected Texture[] colourTextures;
    protected int depthBufferHandle;
    protected int depthStencilBufferHandle;

    protected static ColourAttachmentFormat[] fbCreateFormats;
    protected static IntBuffer attachmentIds;
    protected static final FloatBuffer tmpColours = BufferUtils.newFloatBuffer(4);

    public static MultiTargetFrameBuffer create(Format format, int numColourBuffers, int width, int height, boolean hasDepth, boolean hasStencil) {
        return create(format, null, numColourBuffers, width, height, hasDepth, hasStencil);
    }
    public static MultiTargetFrameBuffer create(Pixmap.Format format, int numColourBuffers, int width, int height, boolean hasDepth, boolean hasStencil) {
        return create(Format.PixmapFormat, format, numColourBuffers, width, height, hasDepth, hasStencil);
    }

    private static MultiTargetFrameBuffer create(Format format, Pixmap.Format pixmapFormat, int numColourBuffers, int width, int height, boolean hasDepth, boolean hasStencil) {
        fbCreateFormats = new ColourAttachmentFormat[numColourBuffers];
        for(int i = 0; i < numColourBuffers; i++)
            fbCreateFormats[i] = new ColourAttachmentFormat(format, pixmapFormat);
        return new MultiTargetFrameBuffer(width, height, hasDepth, hasStencil);
    }

    public static MultiTargetFrameBuffer create(ColourAttachmentFormat[] formats, int width, int height, boolean hasDepth, boolean hasStencil) {
        fbCreateFormats = formats;
        return new MultiTargetFrameBuffer(width, height, hasDepth, hasStencil);
    }

    protected MultiTargetFrameBuffer(int width, int height, boolean hasDepth, boolean hasStencil) {
        super(Pixmap.Format.RGB888, width, height, false, false);
        build(hasDepth, hasStencil);
    }

    public int bufferCount() {
        return colourTextures.length;
    }

    protected void build(boolean hasDepth, boolean hasStencil) {
        bind();
        int numColourAttachments = fbCreateFormats.length;
        colourTextures = new Texture[numColourAttachments];
        colourTextures[0] = colorTexture;
        for(int i = 1; i < numColourAttachments; i++) {
            colourTextures[i] = createColorTexture(i);
            gl30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, colourTextures[i].getTextureObjectHandle(), 0);
        }

        synchronized (MultiTargetFrameBuffer.class) {
            if(attachmentIds == null || numColourAttachments > attachmentIds.capacity()) {
                attachmentIds = BufferUtils.newIntBuffer(numColourAttachments);
                for(int i = 0; i < numColourAttachments; i++)
                    attachmentIds.put(i, GL_COLOR_ATTACHMENT0 + i);
            }
            gl30.glDrawBuffers(numColourAttachments, attachmentIds);
        }

        if(hasStencil) {
            depthStencilBufferHandle = gl30.glGenRenderbuffer();
            gl30.glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBufferHandle);
            gl30.glRenderbufferStorage(GL_RENDERER, GL_DEPTH24_STENCIL8, width, height);
            gl30.glBindRenderbuffer(GL_RENDERER, 0);
            gl30.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthStencilBufferHandle);
        }else if(hasDepth) {

            depthBufferHandle = gl30.glGenTexture();

            gl30.glBindTexture(GL_TEXTURE_2D, depthBufferHandle);
            gl30.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, width, height, 0,
                    GL_DEPTH_COMPONENT, GL_FLOAT, null);

            gl30.glBindTexture(GL_TEXTURE_2D, 0);

            gl30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                    GL_TEXTURE_2D, depthBufferHandle, 0);
        }

        // check status again

        int result = gl30.glCheckFramebufferStatus(GL_FRAMEBUFFER);

        unbind();

        if (result != GL_FRAMEBUFFER_COMPLETE) {
            dispose();
            throw new IllegalStateException("frame buffer couldn'value be constructed: error " + result);
        }
    }

    public void forEach(Consumer<Texture> tex) {
        for (Texture texture : colourTextures)
            tex.accept(texture);
    }
    public void forEachIndexed(BiConsumer<Texture, Integer> func) {
        for (int i = 0; i < colourTextures.length; i++)
            func.accept(colourTextures[i], i);
    }

    @Override
    protected Texture createColorTexture() {
        return createColorTexture(0);
    }

    private Texture createColorTexture(int index) {
        Texture result;

        ColourAttachmentFormat format = fbCreateFormats[index];

        if (format.format == Format.PixmapFormat) {
            int glFormat = Pixmap.Format.toGlFormat(format.pixmapFormat);
            int glType = Pixmap.Format.toGlType(format.pixmapFormat);
            GLOnlyTextureData data = new GLOnlyTextureData(width, height, 0, glFormat, glFormat, glType);
            result = new Texture(data);
        } else {
            ColorBufferTextureData data = new ColorBufferTextureData(format.format, format.generateMipmaps, width, height);
            result = new Texture(data);
        }

        result.setFilter(format.minFilter, format.magFilter);
        result.setWrap(format.wrap, format.wrap);

        return result;
    }
    private Texture createColourTexture(int index, Texture tex) {
        ColourAttachmentFormat format = fbCreateFormats[0];

        if (format.format == Format.PixmapFormat) {
            int glFormat = Pixmap.Format.toGlFormat(format.pixmapFormat);
            int glType = Pixmap.Format.toGlType(format.pixmapFormat);
            GLOnlyTextureData data = new GLOnlyTextureData(width, height, 0, glFormat, glFormat, glType);
            ReflectionUtils.setValue(tex, "data", data);
        } else {
            ColorBufferTextureData data = new ColorBufferTextureData(format.format, format.generateMipmaps, width, height);
            ReflectionUtils.setValue(tex, "data", data);
        }
        return tex;
    }

    @Override
    protected void disposeColorTexture(Texture colorTexture) {
        if(colourTextures != null) {
            for (Texture texture : colourTextures) {
                if (texture != null)
                    texture.dispose();
            }
        }

        if (depthBufferHandle != 0) {
            gl30.glDeleteTexture(depthBufferHandle);
        }

        if (depthStencilBufferHandle != 0) {
            gl30.glDeleteRenderbuffer(depthStencilBufferHandle);
        }
    }

    public Texture getColorBufferTexture(int index) {
        index = MathUtils.clamp(index, 0, colourTextures.length-1);
        return colourTextures[index];
    }

    public void clampToBorder(int index, Color color) {
        int handle = colourTextures[index].getTextureObjectHandle();
        gl30.glBindTexture(GL_TEXTURE_2D, handle);

        gl30.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        gl30.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

        synchronized (tmpColours) {
            tmpColours.clear();
            tmpColours.put(color.r);
            tmpColours.put(color.g);
            tmpColours.put(color.b);
            tmpColours.put(color.a);
            tmpColours.flip();

            gl30.glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, tmpColours);
        }

        gl30.glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void generateMipmap(int index) {
        int handle = colourTextures[index].getTextureObjectHandle();
        gl30.glBindTexture(GL_TEXTURE_2D, handle);
        gl30.glGenerateMipmap(GL_TEXTURE_2D);
        gl30.glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void clearColorBuffer(Color color, int index) {
        clearColorBuffer(color.r, color.g, color.b, color.a, index);
    }

    public void clearColorBuffer(float r, float g, float b, float a, int index) {
        synchronized (tmpColours) {
            tmpColours.clear();
            tmpColours.put(r);
            tmpColours.put(g);
            tmpColours.put(b);
            tmpColours.put(a);
            tmpColours.flip();

            gl30.glClearBufferfv(GL_COLOR, index, tmpColours);
        }
    }

    public void clearColorBuffers(Color color) {
        clearColorBuffers(color.r, color.g, color.b, color.a);
    }

    public void clearColorBuffers(float r, float g, float b, float a) {
        synchronized (tmpColours) {
            tmpColours.clear();
            tmpColours.put(r);
            tmpColours.put(g);
            tmpColours.put(b);
            tmpColours.put(a);
            tmpColours.flip();

            for (int index = 0; index < colourTextures.length; index++) {
                gl30.glClearBufferfv(GL_COLOR, index, tmpColours);
            }
        }
    }

    public void clearColorBuffers(Color color, int[] indices) {
        clearColorBuffers(color.r, color.g, color.b, color.a, indices);
    }

    public void clearColorBuffers(float r, float g, float b, float a, int[] indices) {
        synchronized (tmpColours) {
            tmpColours.clear();
            tmpColours.put(r);
            tmpColours.put(g);
            tmpColours.put(b);
            tmpColours.put(a);
            tmpColours.flip();

            for (int index : indices) {
                gl30.glClearBufferfv(GL_COLOR, index, tmpColours);
            }
        }
    }

    public void clearDepthBuffer(float depth) {
        synchronized (tmpColours) {
            tmpColours.clear();
            tmpColours.put(depth);
            tmpColours.flip();

            gl30.glClearBufferfv(GL_DEPTH, 0, tmpColours);
        }
    }

    public void clearDepthStencilBuffer(float depth, int stencil) {
        gl30.glClearBufferfi(GL_DEPTH_STENCIL, 0, depth, stencil);
    }

    public int getId(int index) {
        index = MathUtils.clamp(index, 0, attachmentIds.capacity()-1);
        return attachmentIds.get(index);
    }

    public void overrideBufferContents(int id, Texture texture) {
        texture = createColourTexture(id, texture);
        if(id == 0) colorTexture = texture;
        colourTextures[id] = texture;
        gl30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + id, GL_TEXTURE_2D, colourTextures[id].getTextureObjectHandle(), 0);
    }

    private static class ColorBufferTextureData implements TextureData {

        private final Format format;
        private final int width;
        private final int height;
        private final boolean generateMipmap;

        private boolean isPrepared = false;

        ColorBufferTextureData(Format format, boolean generateMipmap, int width, int height) {
            this.format = format;
            this.generateMipmap = generateMipmap;
            this.width = width;
            this.height = height;
        }

        @Override
        public TextureData.TextureDataType getType() {
            return TextureDataType.Custom;
        }

        @Override
        public boolean isPrepared() {
            return isPrepared;
        }

        @Override
        public void prepare() {
            isPrepared = true;
        }

        @Override
        public Pixmap consumePixmap() {
            return null;
        }

        @Override
        public boolean disposePixmap() {
            return false;
        }

        @Override
        public void consumeCustomData(int target) {
            gl30.glTexImage2D(target, 0, format.internal, width, height, 0, format.format, format.type, null);
            if (generateMipmap) {
                gl30.glGenerateMipmap(target);
            }
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public Pixmap.Format getFormat() {
            return null;
        }

        @Override
        public boolean useMipMaps() {
            return generateMipmap;
        }

        @Override
        public boolean isManaged() {
            return true;
        }
    }

    public static void copyDepthStencilBuffer(MultiTargetFrameBuffer target,
                                              int destX0, int destY0, int destX1, int destY1,
                                              MultiTargetFrameBuffer source,
                                              int srcX0, int srcY0, int srcX1, int srcY1) {

        int mask = GL_DEPTH_BUFFER_BIT;

        if (source.hasStencil && target.hasStencil) {
            mask |= GL_STENCIL_BUFFER_BIT;
        }

        int sourceFbo = source.getFramebufferHandle();
        int targetFbo = target.getFramebufferHandle();

        gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, sourceFbo);
        gl30.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, targetFbo);

        gl30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, destX0, destY0, destX1, destY1, mask, GL_NEAREST);

        gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        gl30.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    public enum Format {
        R32F(GL_R32F, GL_RED, GL_FLOAT),
        RG32F(GL_RG32F, GL_RG, GL_FLOAT),
        RGB32F(GL_RGB32F, GL_RGB, GL_FLOAT),
        RGBA32F(GL_RGBA32F, GL_RGBA, GL_FLOAT),

        RG16F(GL_RG16F, GL_RG, GL_FLOAT),

        R8(GL_R8, GL_RED, GL_UNSIGNED_BYTE),
        RG8(GL_RG8, GL_RG, GL_UNSIGNED_BYTE),

        R32I(GL_R32I, GL_RED_INTEGER, GL_INT),

        PixmapFormat(GL_NONE, GL_NONE, GL_NONE),
        ;
        private final int internal, format, type;
        Format(int internal, int format, int type) {
            this.internal = internal;
            this.format = format;
            this.type = type;
        }
    }

    public static class ColourAttachmentFormat {
        Format format = Format.PixmapFormat;
        Pixmap.Format pixmapFormat = Pixmap.Format.RGB888;
        boolean generateMipmaps = false;
        Texture.TextureFilter minFilter = Texture.TextureFilter.Nearest;
        Texture.TextureFilter magFilter = Texture.TextureFilter.Nearest;
        Texture.TextureWrap wrap = Texture.TextureWrap.ClampToEdge;

        public ColourAttachmentFormat(Format format, Pixmap.Format pixmapFormat) {
            this.format = format;
            this.pixmapFormat = pixmapFormat;
        }

        public ColourAttachmentFormat(Format format, Pixmap.Format pixmapFormat, boolean generateMipmaps, Texture.TextureFilter minFilter, Texture.TextureFilter magFilter) {
            this.format = format;
            this.pixmapFormat = pixmapFormat;
            this.generateMipmaps = generateMipmaps;
            this.minFilter = minFilter;
            this.magFilter = magFilter;
        }

        public ColourAttachmentFormat(Format format, Pixmap.Format pixmapFormat, boolean generateMipmaps, Texture.TextureFilter minFilter, Texture.TextureFilter magFilter, Texture.TextureWrap wrap) {
            this.format = format;
            this.pixmapFormat = pixmapFormat;
            this.generateMipmaps = generateMipmaps;
            this.minFilter = minFilter;
            this.magFilter = magFilter;
            this.wrap = wrap;
        }
    }

}
