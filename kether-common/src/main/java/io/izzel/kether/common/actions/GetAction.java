package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

public class GetAction<T> extends QuestAction<T> {

    private final String key;

    public GetAction(String key) {
        this.key = key;
    }

    @Override
    public CompletableFuture<T> process(QuestContext.Frame frame) {
        return CompletableFuture.completedFuture(frame.variables().<T>get(key).orElse(null));
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(
            resolver -> new GetAction<>(resolver.nextToken()),
            KetherCompleters.consume()
        );
    }
}
