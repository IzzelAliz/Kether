package io.izzel.kether.common.loader;

import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.data.ContextString;
import io.izzel.kether.common.loader.types.ArgTypes;
import io.izzel.kether.common.util.LocalizedException;

import java.util.Optional;
import java.util.function.Supplier;

public interface QuestReader {

    char peek();

    char peek(int n);

    int getIndex();

    int getMark();

    void setIndex(int index);

    boolean hasNext();

    String nextToken();

    ContextString nextString();

    void mark();

    void reset();

    <T> QuestAction<T> nextAction();

    default void expect(String value) {
        String element = nextToken();
        if (!element.equals(value)) {
            throw LocalizedException.of("not-match", value, element);
        }
    }

    default int nextInt() {
        return next(ArgTypes.INT);
    }

    default long nextLong() {
        return next(ArgTypes.LONG);
    }

    default double nextDouble() {
        return next(ArgTypes.DOUBLE);
    }

    boolean flag(String name);

    <T> Optional<T> optionalFlag(String name, ArgType<T> flagType);

    default <T> T flag(String name, ArgType<T> flagType, Supplier<T> defaultValue) {
        return optionalFlag(name, flagType).orElseGet(defaultValue);
    }

    default <T> T next(ArgType<T> argType) throws LocalizedException {
        return argType.read(this);
    }
}
