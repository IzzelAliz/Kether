package io.izzel.kether.common.persistent;

import io.izzel.kether.common.util.LocalizedException;

import java.util.Locale;

public enum PersistentLoadError {
    UNEXPECTED_REWRITE_END,
    RECURSIVE_REWRITE;

    public LocalizedException create(Object... args) {
        return LocalizedException.of("load-error.persistent." + name().toLowerCase(Locale.ROOT), args);
    }
}
