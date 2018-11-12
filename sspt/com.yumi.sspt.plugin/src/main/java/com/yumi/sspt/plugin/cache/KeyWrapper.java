package com.yumi.sspt.plugin.cache;

import java.util.Objects;
import java.util.function.Function;

/**
 * KeyWrapper
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 */
public class KeyWrapper<T, R> {

    private String key;

    private Function<T, R> func;


    public String getKey() {
        return key;
    }

    public KeyWrapper<T, R> setKey(String key) {
        this.key = key;
        return this;
    }

    public Function<T, R> getFunc() {
        return func;
    }

    public KeyWrapper<T, R> setFunc(Function<T, R> func) {
        this.func = func;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyWrapper<?, ?> that = (KeyWrapper<?, ?>) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "KeyWrapper{" +
                "key='" + key + '\'' +
                '}';
    }
}
