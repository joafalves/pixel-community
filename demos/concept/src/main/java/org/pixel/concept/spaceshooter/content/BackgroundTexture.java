package org.pixel.concept.spaceshooter.content;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11C.GL_REPEAT;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.glGetTexImage;
import static org.lwjgl.system.libc.LibCStdlib.free;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.pixel.content.Texture;
import org.pixel.content.TextureFrame;

public class BackgroundTexture extends Texture {

    /**
     * Constructor
     */
    public BackgroundTexture() {
        super(glGenTextures());
    }

    public void setData(Texture baseTexture, List<TextureFrame> frames, int blocksX, int blocksY) {
        // assuming that all frames have the same size...
        final float fw = frames.get(0).getSource().getWidth();
        final float fh = frames.get(0).getSource().getHeight();
        final float tw = fw * blocksX;
        final float th = fh * blocksY;
        final ByteBuffer originalData = BufferUtils.createByteBuffer(
                (int) (baseTexture.getWidth() * baseTexture.getHeight() * 4));
        final ByteBuffer data = BufferUtils.createByteBuffer((int) (tw * th * 4));

        baseTexture.bind();
        glGetTexImage(GL11C.GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, originalData);
        baseTexture.unbind();

        for (int i = 0; i < blocksX * blocksY; i++) {
            TextureFrame frame = frames.get(ThreadLocalRandom.current().nextInt(frames.size()));
            int ex = (int) (baseTexture.getWidth() * 4.0);
            for (int x = (int) frame.getSource().getX(); x < frame.getSource().getX() + frame.getSource().getWidth(); x++) {
                for (int y = (int) frame.getSource().getY(); y < frame.getSource().getY() + frame.getSource().getHeight(); y++) {
                    int oxy = y * ex + x * 4;
                    data.put(originalData.get(oxy));
                    data.put(originalData.get(oxy + 1));
                    data.put(originalData.get(oxy + 2));
                    data.put(originalData.get(oxy + 3));
                }
            }
        }

        bind();
        setTextureWrap(GL_REPEAT, GL_REPEAT);
        setTextureMinMag(GL_NEAREST, GL_NEAREST);
        setData(data.flip(), (int) tw, (int) th);
        unbind();

        free(originalData);
        free(data);
    }
}
