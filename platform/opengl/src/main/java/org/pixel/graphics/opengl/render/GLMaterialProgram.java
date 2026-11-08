package org.pixel.graphics.opengl.render;

import org.pixel.graphics.backend.MaterialProgram;
import org.pixel.graphics.material.Material;
import org.pixel.graphics.opengl.GLApi;
import org.pixel.graphics.opengl.GLProgram;

import java.util.HashMap;
import java.util.Map;

/**
 * A material's compiled program: the batch program state plus its uniform locations and the material values last
 * uploaded.
 */
final class GLMaterialProgram extends ProgramState implements MaterialProgram {

    final int generation; // context generation it was made in
    final int firstMaterialUnit; // material texture i is bound to unit firstMaterialUnit + i
    private final Map<String, Integer> locations = new HashMap<>();
    private int uploadedSerial = Integer.MIN_VALUE;
    private boolean disposed;

    GLMaterialProgram(GLApi gl, GLProgram program, int textureSlots, int generation) {
        super(gl, program, textureSlots);
        this.generation = generation;
        this.firstMaterialUnit = textureSlots;
    }

    /**
     * Upload the material's uniforms and sampler units if they changed since the last upload (must be bound).
     */
    void upload(Material material) {
        int serial = material.getValuesSerial();
        if (serial == uploadedSerial) {
            return;
        }
        uploadedSerial = serial;
        for (int u = 0, n = material.getUniformCount(); u < n; u++) {
            int location = location(material.getUniformName(u));
            if (location < 0) {
                continue; // not declared, or optimized away
            }
            switch (material.getUniformSize(u)) {
                case 1 -> gl.uniform1f(location, material.getUniformValue(u, 0));
                case 2 -> gl.uniform2f(location, material.getUniformValue(u, 0), material.getUniformValue(u, 1));
                default -> gl.uniform4f(location, material.getUniformValue(u, 0), material.getUniformValue(u, 1),
                        material.getUniformValue(u, 2), material.getUniformValue(u, 3));
            }
        }
        for (int t = 0, n = material.getTextureCount(); t < n; t++) {
            int location = location(material.getTextureName(t));
            if (location >= 0) {
                gl.uniform1i(location, firstMaterialUnit + t);
            }
        }
    }

    private int location(String name) {
        Integer location = locations.get(name);
        if (location == null) {
            location = program.uniformLocation(name);
            locations.put(name, location);
        }
        return location;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            super.dispose();
        }
    }
}
