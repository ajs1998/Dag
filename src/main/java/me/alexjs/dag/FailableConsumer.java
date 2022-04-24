package me.alexjs.dag;

public interface FailableConsumer<T> {

    void accept(T value) throws Throwable;

}
