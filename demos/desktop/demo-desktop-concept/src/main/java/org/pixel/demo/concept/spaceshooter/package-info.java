/**
 * Space Shooter: a horizontal shoot 'em up built on the engine's render pipeline (URP), as a complete small game.
 * <p>
 * Layout:
 * <ul>
 *   <li>{@code SpaceShooterGame}: the entry point (window settings, screens, per-frame wiring);
 *   {@code Stage}: what the screens share (render pipeline, world, effects, UI); {@code GameConfig}: fixed
 *   numbers.</li>
 *   <li>{@code world}: the simulation (player, enemies and their AI, shots, pickups, score). No drawing, no
 *   devices, no allocation while playing; it reports events to a {@code WorldListener}. Unit tested.</li>
 *   <li>{@code wave}: the wave director and its formations.</li>
 *   <li>{@code fx}: effects driven by world events (particles, sprite-sheet explosions, popups, screen shake, the
 *   shockwave post effect).</li>
 *   <li>{@code render}: layers, palette, the parallax starfield and the world renderer.</li>
 *   <li>{@code ui}: the HUD and the menus.</li>
 *   <li>{@code screen}: title (with an attract mode) and play (pause, game over).</li>
 *   <li>{@code input}: device-independent controls, the keyboard bindings and the autopilot.</li>
 *   <li>{@code assets}: loading and pixel-art drawing helpers.</li>
 *   <li>{@code debug}: the dev tools page (F1), shader hot reload and screenshot capture.</li>
 * </ul>
 * Assets live in {@code resources/spaceshooter/}: {@code sprites/}, {@code fx/}, {@code fonts/}, {@code shaders/}.
 * <p>
 * Engine features exercised: layers (sorted, ordered, additive), a screen target with post effects (bloom, vignette
 * and a custom {@code ShaderEffect}), an effect material on every ship (hit flash, one draw call), canvas shapes,
 * gradients, paths and text, sprite sheets, the dev tools, and {@code SourceWatcher} hot reload.
 */
package org.pixel.demo.concept.spaceshooter;
