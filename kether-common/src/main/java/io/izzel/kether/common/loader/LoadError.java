package io.izzel.kether.common.loader;

import io.izzel.kether.common.util.LocalizedException;

import java.util.Locale;

public enum LoadError {
    STRING_NOT_CLOSE,
    NOT_MATCH,
    UNKNOWN_ACTION,
    NOT_DURATION,
    EOF,
    BLOCK_ERROR,
    UNHANDLED;

    public LocalizedException create(Object... args) {
        return LocalizedException.of(name().toLowerCase(Locale.ROOT), args);
    }
}
