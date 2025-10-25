package ru.hollowhorizon.additions.questing.client;

import org.lwjgl.glfw.GLFW;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

import java.util.function.Function;

public class Animator {
    private final float duration;
    private final Function<Float, Float> interpolator;
    private float fromValue;
    private float toValue;
    private double lastSetTime;
    private float value;

    public Animator(float startValue, float duration, Function<Float, Float> interpolator) {
        double startTime = getCurrentTime();
        this.duration = duration;
        this.interpolator = interpolator != null ? interpolator : t -> t;
        this.fromValue = startValue;
        this.toValue = startValue;
        this.lastSetTime = startTime;
        this.value = startValue;
    }

    public Animator(float startValue, float duration) {
        this(startValue, duration, null);
    }

    public void set(float target, Float from) {
        if (toValue == target && from == null) {
            return;
        }
        this.fromValue = from != null ? from : get();
        this.toValue = target;
        this.lastSetTime = getCurrentTime();
    }

    public void set(float target) {
        set(target, null);
    }

    public float get() {
        return value;
    }

    public void update() {
        double now = getCurrentTime();
        float t = (float) Math.min(Math.max((now - lastSetTime) / duration, 0.0), 1.0);
        float k = QuestAnimationsConfig.SMOOTH_ANIMATIONS.get() ? interpolator.apply(t) : t;
        value = fromValue + (toValue - fromValue) * k;
    }

    public void add(float value) {
        set(get() + value);
    }

    public boolean isAnimating() {
        double now = getCurrentTime();
        return (now - lastSetTime) < duration;
    }

    private double getCurrentTime() {
        return GLFW.glfwGetTime();
    }

    public float getToValue() {
        return toValue;
    }

    public float getFromValue() {
        return fromValue;
    }
}
