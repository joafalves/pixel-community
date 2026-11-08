package org.pixel.graphics.opengl.render;

import org.pixel.graphics.backend.PostProgram;
import org.pixel.graphics.opengl.GLApi;
import org.pixel.graphics.opengl.GLProgram;

import java.util.HashMap;
import java.util.Map;

/**
 * A full-screen effect: one triangle generated from {@code gl_VertexID} (no vertex buffers), and the effect's
 * {@code vec4 effect(vec2 uv)} wrapped in a fragment shader that declares the standard inputs.
 */
final class GLPostProgram extends GLProgram implements PostProgram {

    private static final String VERTEX = """
            uniform float uFlipY; // 1 when writing to the screen (rows bottom-up), 0 for targets (rows top-down)
            out vec2 vUv;
            void main() {
                vec2 p = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2)); // (0,0) (2,0) (0,2)
                vUv = vec2(p.x, mix(p.y, 1.0 - p.y, uFlipY));
                gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0);
            }
            """;

    private static final String HEADER = """
            uniform sampler2D uSource;   // the image being processed (premultiplied)
            uniform sampler2D uInput1;
            uniform sampler2D uInput2;
            uniform sampler2D uInput3;
            uniform vec2 uTexel;         // 1 / source size in pixels
            uniform vec2 uResolution;    // destination size in pixels
            in vec2 vUv;
            out vec4 oFragColor;
            """;

    private static final String[] SAMPLERS = {"uSource", "uInput1", "uInput2", "uInput3"};

    private final Map<String, Integer> locations = new HashMap<>();
    final int flipYLocation;
    final int texelLocation;
    final int resolutionLocation;

    GLPostProgram(GLApi gl, String effectSource) {
        super(gl, VERTEX, HEADER + effectSource + "\nvoid main() {\n    oFragColor = effect(vUv);\n}\n");
        bind();
        for (int i = 0; i < SAMPLERS.length; i++) {
            int location = uniformLocation(SAMPLERS[i]);
            if (location >= 0) {
                gl.uniform1i(location, i); // uSource on unit 0, uInputN on unit N
            }
        }
        unbind();
        flipYLocation = uniformLocation("uFlipY");
        texelLocation = uniformLocation("uTexel");
        resolutionLocation = uniformLocation("uResolution");
    }

    /**
     * Location of an effect uniform (cached; -1 if the effect doesn't use it).
     */
    int location(String name) {
        Integer location = locations.get(name);
        if (location == null) {
            location = uniformLocation(name);
            locations.put(name, location);
        }
        return location;
    }
}
