package io.disassemble.knn.util;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public interface SourceEditor<T, V> {

    V edit(T value);
}
