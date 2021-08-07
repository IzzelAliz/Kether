package io.izzel.kether.common.loader.types;

import io.izzel.kether.common.loader.ArgType;
import io.izzel.kether.common.loader.QuestReader;
import io.izzel.kether.common.util.LocalizedException;

import java.util.Optional;
import java.util.function.Function;

public class CoerceType<T> implements ArgType<T> {

    private final Function<Object, Optional<T>> function;
    private final String type;

    CoerceType(Function<Object, Optional<T>> function, String type) {
        this.function = function;
        this.type = type;
    }

    @Override
    public T read(QuestReader reader) throws LocalizedException {
        String token = reader.nextToken();
        return function.apply(token).orElseThrow(LocalizedException.supply("not_" + type, token));
    }
}
