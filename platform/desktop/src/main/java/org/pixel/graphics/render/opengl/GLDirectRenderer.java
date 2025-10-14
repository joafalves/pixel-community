package org.pixel.graphics.render.opengl;

import org.lwjgl.system.MemoryUtil;
import org.pixel.commons.data.DataMap;
import org.pixel.content.Texture;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.render.DirectRenderer;
import org.pixel.graphics.render.Renderable;
import org.pixel.graphics.render.SpriteRenderable;
import org.pixel.graphics.shader.opengl.GLShader;
import org.pixel.graphics.shader.opengl.GLVertexArrayObject;
import org.pixel.graphics.shader.opengl.GLVertexBufferObject;
import org.pixel.math.Matrix4;
import org.pixel.math.Vector2;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;

/**
 * Internal renderer for drawing single objects with custom shaders, without batching.
 * This renderer performs vertex transformations on the CPU.
 */
public class GLDirectRenderer implements DirectRenderer {

    private static final int VERTEX_COUNT = 6;
    private static final int VERTEX_FLOAT_COUNT = VERTEX_COUNT * 4; // 6 vertices, 4 floats each (x,y,u,v)

    private final GLVertexArrayObject vao;
    private final GLVertexBufferObject vbo;
    private final FloatBuffer vertexBuffer;
    private final Matrix4 modelMatrix;
    private final FloatBuffer matrixBuffer;

    // Reusable vectors for calculation
    private final Vector2 v1 = new Vector2(0, 0); // Top-Left
    private final Vector2 v2 = new Vector2(1, 0); // Top-Right
    private final Vector2 v3 = new Vector2(0, 1); // Bottom-Left
    private final Vector2 v4 = new Vector2(1, 1); // Bottom-Right

    /**
     * Constructor.
     */
    public GLDirectRenderer() {
        this.vao = new GLVertexArrayObject();
        this.vbo = new GLVertexBufferObject();
        this.modelMatrix = new Matrix4();
        this.matrixBuffer = MemoryUtil.memAllocFloat(16);
        this.vertexBuffer = MemoryUtil.memAllocFloat(VERTEX_FLOAT_COUNT);
    }

    @Override
    public boolean init() {
        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        // Allocate buffer on GPU, but upload data per-draw
        vbo.uploadData(GL_ARRAY_BUFFER, (long) VERTEX_FLOAT_COUNT * Float.BYTES, GL_DYNAMIC_DRAW);

        // Vertex positions (pre-transformed)
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        // Texture coordinates
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        vao.unbind();
        return true;
    }

    @Override
    public void draw(Renderable renderable, Matrix4 viewMatrix) {
        // DirectRenderer only works with SpriteRenderable (for now)
        if (!(renderable instanceof SpriteRenderable sprite)) {
            return;
        }

        GLShader shader = (GLShader) sprite.getShader();
        if (shader == null) return;

        shader.bind();
        vao.bind();

        // Calculate final sprite dimensions (matching GLFastSpriteBatch behavior)
        float finalWidth, finalHeight;
        if (sprite.getTexture() != null) {
            Texture tex = sprite.getTexture();
            if (sprite.getSource() != null) {
                finalWidth = sprite.getSource().getWidth() * sprite.getScale().getX();
                finalHeight = sprite.getSource().getHeight() * sprite.getScale().getY();
            } else {
                finalWidth = tex.getWidth() * sprite.getScale().getX();
                finalHeight = tex.getHeight() * sprite.getScale().getY();
            }
        } else {
            finalWidth = sprite.getScale().getX();
            finalHeight = sprite.getScale().getY();
        }

        // Build model matrix matching GPU shader logic (instanced_multitex.vert.glsl)
        // 1. Scale unit quad to sprite size
        // 2. Subtract anchor offset (so anchor point is at origin)
        // 3. Rotate around origin (anchor point)
        // 4. Translate to final position
        modelMatrix.setIdentity();
        modelMatrix.translate(sprite.getPosition().getX(), sprite.getPosition().getY(), 0);
        if (sprite.getRotation() != 0) {
            modelMatrix.rotate(sprite.getRotation(), 0, 0, 1);
        }
        modelMatrix.translate(-sprite.getAnchor().getX() * finalWidth, -sprite.getAnchor().getY() * finalHeight, 0);
        modelMatrix.scale(finalWidth, finalHeight, 1);

        // 2. Transform quad vertices on the CPU
        v1.set(0, 0); v1.transformMatrix4(modelMatrix);
        v2.set(1, 0); v2.transformMatrix4(modelMatrix);
        v3.set(0, 1); v3.transformMatrix4(modelMatrix);
        v4.set(1, 1); v4.transformMatrix4(modelMatrix);

        // 3. Upload vertex data for this one sprite
        vertexBuffer.clear();
        addVertex(v1, 0, 0); addVertex(v2, 1, 0); addVertex(v3, 0, 1); // Triangle 1
        addVertex(v3, 0, 1); addVertex(v2, 1, 0); addVertex(v4, 1, 1); // Triangle 2
        vertexBuffer.flip();

        vbo.bind(GL_ARRAY_BUFFER);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

        // 4. Upload uniforms
        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        glUniformMatrix4fv(shader.getUniformLocation("uMatrix"), false, matrixBuffer);

        int texUniformLocation = shader.getUniformLocation("uTextureImage");
        if (texUniformLocation >= 0) {
            glUniform1i(texUniformLocation, 0);
        }
        uploadCustomUniforms(shader, sprite.getShaderData());

        // 5. Bind texture
        Texture texture = sprite.getTexture();
        if (texture instanceof GLTexture) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, ((GLTexture) texture).getId());
        }

        // 6. Draw call
        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);

        vao.unbind();
    }

    private void addVertex(Vector2 pos, float u, float v) {
        vertexBuffer.put(pos.getX());
        vertexBuffer.put(pos.getY());
        vertexBuffer.put(u);
        vertexBuffer.put(v);
    }

    private void uploadCustomUniforms(GLShader shader, DataMap uniforms) {
        if (uniforms == null) return;

        for (var entry : uniforms.entrySet()) {
            int location = shader.getUniformLocation(entry.getKey());
            if (location < 0) continue;

            Object value = entry.getValue();
            if (value instanceof Float) {
                glUniform1f(location, (Float) value);
            } else if (value instanceof Integer) {
                glUniform1i(location, (Integer) value);
            } else if (value instanceof Vector2) {
                glUniform2f(location, ((Vector2) value).getX(), ((Vector2) value).getY());
            }
        }
    }

    @Override
    public void dispose() {
        vao.dispose();
        vbo.dispose();
        MemoryUtil.memFree(matrixBuffer);
        MemoryUtil.memFree(vertexBuffer);
    }
}
