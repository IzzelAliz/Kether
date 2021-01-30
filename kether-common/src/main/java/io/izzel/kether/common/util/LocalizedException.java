package io.izzel.kether.common.util;

import io.izzel.kether.common.api.QuestService;

import java.util.function.Supplier;

public class LocalizedException extends RuntimeException {

    private final String node;
    private final Object[] params;

    public LocalizedException(String node, Object[] params) {
        this.node = "load-error." + node;
        this.params = params;
    }

    public String getNode() {
        return node;
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }

    @Override
    public String getLocalizedMessage() {
        return QuestService.instance().getLocalizedText(node, params);
    }

    public static LocalizedException of(String node, Object... params) {
        return new LocalizedException(node, params);
    }

    public static Supplier<LocalizedException> supply(String node, Object... params) {
        return () -> of(node, params);
    }
}
