package org.pixel.ext.tween;

import org.pixel.ext.tween.easing.*;

import java.util.HashMap;

public class TweenEasingFactory {

    private static final HashMap<String, TweenEasingExecutor> easingMap;

    static {
        // default easing executors:
        easingMap = new HashMap<>();
        easingMap.put(TweenEasingMode.LINEAR.name(), new LinearTweenEasingExecutor());

        easingMap.put(TweenEasingMode.QUADRATIC.name(), new QuadraticEasingExecutor());
        easingMap.put(TweenEasingMode.QUADRATIC_IN.name(), new QuadraticInEasingExecutor());
        easingMap.put(TweenEasingMode.QUADRATIC_OUT.name(), new QuadraticOutEasingExecutor());

        easingMap.put(TweenEasingMode.CUBIC.name(), new CubicEasingExecutor());
        easingMap.put(TweenEasingMode.CUBIC_IN.name(), new CubicInEasingExecutor());
        easingMap.put(TweenEasingMode.CUBIC_OUT.name(), new CubicOutEasingExecutor());

        easingMap.put(TweenEasingMode.SIN.name(), new SinusoidalEasingExecutor());
        easingMap.put(TweenEasingMode.SIN_IN.name(), new SinusoidalInEasingExecutor());
        easingMap.put(TweenEasingMode.SIN_OUT.name(), new SinusoidalOutEasingExecutor());

        easingMap.put(TweenEasingMode.EXPONENTIAL.name(), new ExponentialEasingExecutor());
        easingMap.put(TweenEasingMode.EXPONENTIAL_IN.name(), new ExponentialInEasingExecutor());
        easingMap.put(TweenEasingMode.EXPONENTIAL_OUT.name(), new ExponentialOutEasingExecutor());

        easingMap.put(TweenEasingMode.CIRCULAR.name(), new CircularEasingExecutor());
        easingMap.put(TweenEasingMode.CIRCULAR_IN.name(), new CircularInEasingExecutor());
        easingMap.put(TweenEasingMode.CIRCULAR_OUT.name(), new CircularOutEasingExecutor());

        easingMap.put(TweenEasingMode.BOUNCE.name(), new BounceEasingExecutor());
        easingMap.put(TweenEasingMode.BOUNCE_IN.name(), new BounceInEasingExecutor());
        easingMap.put(TweenEasingMode.BOUNCE_OUT.name(), new BounceOutEasingExecutor());

        easingMap.put(TweenEasingMode.ELASTIC.name(), new ElasticEasingExecutor());
        easingMap.put(TweenEasingMode.ELASTIC_IN.name(), new ElasticInEasingExecutor());
        easingMap.put(TweenEasingMode.ELASTIC_OUT.name(), new ElasticOutEasingExecutor());
    }

    /**
     * Get the executor for the given easing mode.
     *
     * @param easing The easing mode.
     * @return The executor or null if it doesn't exist.
     */
    public static TweenEasingExecutor getExecutor(TweenEasingMode easing) {
        return getExecutor(easing.name());
    }

    /**
     * Get the executor for the given easing mode.
     *
     * @param executorIdentity The executor identity.
     * @return The executor or null if it doesn't exist.
     */
    public static TweenEasingExecutor getExecutor(String executorIdentity) {
        return easingMap.get(executorIdentity);
    }

    /**
     * Put a given executor in the map.
     *
     * @param executorIdentity The executor identity.
     * @param executor         The executor instance.
     */
    public static void putExecutor(String executorIdentity, TweenEasingExecutor executor) {
        easingMap.put(executorIdentity, executor);
    }

    /**
     * Remove a given executor from the map.
     *
     * @param executorIdentity The executor identity.
     * @return The executor instance or null if it doesn't exist.
     */
    public static TweenEasingExecutor removeExecutor(String executorIdentity) {
        return easingMap.remove(executorIdentity);
    }
}
