package org.pixel.demo.concept.spaceshooter.content;

import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_REPEAT;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glGetTexImage;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.system.libc.LibCStdlib.free;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.pixel.content.Texture;
import org.pixel.content.TextureFrame;

public class BackgroundTexture extends Texture {

    public BackgroundTexture() {
        super(glGenTextures());
    }

    public void setData(Texture baseTexture, List<TextureFrame> frames, int blocksX, int blocksY) {
        // assumes that all frames have the same size...
        final float fw = frames.get(0).getSource().getWidth();
        final float fh = frames.get(0).getSource().getHeight();
        final float tw = fw * blocksX;
        final float th = fh * blocksY;
        final ByteBuffer originalData = BufferUtils.createByteBuffer(
                (int) (baseTexture.getWidth() * baseTexture.getHeight() * 4));
        final ByteBuffer data = BufferUtils.createByteBuffer((int) (tw * th * 4));

        glBindTexture(GL_TEXTURE_2D, baseTexture.getId());
        glGetTexImage(GL11C.GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, originalData);
        glBindTexture(GL_TEXTURE_2D, 0);

        frames.sort(Comparator.comparingInt(o -> o.getAttributes().getInteger("weight", 1)));

        for (int i = 0; i < blocksX * blocksY; i++) {
            TextureFrame frame = pickTextureFrame(frames);
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

        glBindTexture(GL_TEXTURE_2D, getId());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, (int) tw, (int) th, 0, GL_RGBA, GL_UNSIGNED_BYTE, data.flip());
        glBindTexture(GL_TEXTURE_2D, 0);

        width = tw;
        height = th;

        free(originalData);
        free(data);
    }

    private TextureFrame pickTextureFrame(List<TextureFrame> frames) {
        // pick selection based on texture frame weights (more weight = more probability of being picked)
        // assumes that there are more than one frame in the list
        int totalWeight = 0;
        for (TextureFrame frame : frames) {
            totalWeight += frame.getAttributes().getInteger("weight", 1);
        }

        int weightSum = 0;
        int rnd = ThreadLocalRandom.current().nextInt(totalWeight);
        for (TextureFrame frame : frames) {
            int weight = frame.getAttributes().getInteger("weight", 1);
            if (rnd < weightSum + weight) {
                return frame;
            }
            weightSum += weight;
        }

        throw new RuntimeException("Should not happen");
    }
}
