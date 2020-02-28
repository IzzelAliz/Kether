package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class SetAction<CTX extends QuestContext> implements QuestAction<Void, CTX> {

    private final String key;
    private final String value;

    public SetAction(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        if (value == null || value.equals("null")) {
            context.getPersistentData().remove(key);
        } else {
            context.getPersistentData().put(key, value);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getDataPrefix() {
        return "set";
    }

    @Override
    public String toString() {
        return "SetAction{" +
            "key='" + key + '\'' +
            ", value='" + value + '\'' +
            '}';
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(
            resolver -> new SetAction<>(resolver.nextElement(), resolver.nextElement()),
            KetherCompleters.seq(
                KetherCompleters.consume(),
                KetherCompleters.consume()
            )
        );
    }
}
