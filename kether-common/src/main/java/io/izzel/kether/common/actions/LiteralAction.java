package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.KetherCompleters;

import java.util.concurrent.CompletableFuture;

public class LiteralAction<T> extends QuestAction<T> {

    private final Object value;

    public LiteralAction(Object value) {
        this.value = value;
    }

    public LiteralAction(String value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<T> process(QuestContext.Frame frame) {
        return CompletableFuture.completedFuture((T) value);
    }

    @Override
    public String toString() {
        return "LiteralAction{" +
            "value=" + value +
            '}';
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(
            reader -> new LiteralAction<>(reader.nextToken()),
            KetherCompleters.consume()
        );
    }
}
