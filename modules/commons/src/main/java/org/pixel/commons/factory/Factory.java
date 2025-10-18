package org.pixel.commons.factory;

/**
 * Marker interface for factory implementations.
 *
 * <p>Unlike {@link org.pixel.commons.service.ServiceFactory}, this interface does not impose
 * any specific method signatures. Each factory interface can define its own {@code create()}
 * methods with whatever parameters are appropriate for creating instances of that type.
 *
 * <p>This is designed for types that:
 * <ul>
 *   <li>Require constructor parameters (e.g., Canvas needs width/height)</li>
 *   <li>Are created multiple times rather than being singletons</li>
 *   <li>Need platform-specific instantiation logic</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * public interface CanvasFactory extends Factory {
 *     Canvas create(int width, int height);
 * }
 *
 * // Platform implementation
 * public class GLCanvasFactory implements CanvasFactory {
 *     public Canvas create(int width, int height) {
 *         return new GLCanvas(width, height);
 *     }
 * }
 * </pre>
 */
public interface Factory {
}
