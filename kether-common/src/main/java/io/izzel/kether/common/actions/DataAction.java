package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class DataAction<CTX extends QuestContext> implements QuestAction<Void, CTX> {

    private final String key;

    public DataAction(String key) {
        this.key = key;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        context.setDataStore(key);
        return CompletableFuture.completedFuture(null);
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(
            resolver -> new DataAction<>(resolver.nextElement()),
            KetherCompleters.consume()
        );
    }
}
