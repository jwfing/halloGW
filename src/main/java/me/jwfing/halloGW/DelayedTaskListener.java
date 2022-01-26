package me.jwfing.halloGW;

public interface DelayedTaskListener<T> {
    void invoke(T t);
}
