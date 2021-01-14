package io.izzel.kether.common.loader;

import io.izzel.kether.common.util.LocalizedException;

public interface ArgType<T> {

    T read(QuestReader reader) throws LocalizedException;
}
