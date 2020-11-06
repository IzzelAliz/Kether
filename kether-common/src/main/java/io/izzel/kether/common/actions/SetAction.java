package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class SetAction implements QuestAction<Void> {

    private final String key;
    private final String value;

    public SetAction(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext context) {
        if (value == null || value.equals("null")) {
            context.putLocal(key, null);
        } else {
            context.putLocal(key, value);
        }
        return CompletableFuture.completedFuture(null);
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
            resolver -> new SetAction(resolver.nextElement(), resolver.nextElement()),
            KetherCompleters.seq(
                KetherCompleters.consume(),
                KetherCompleters.consume()
            )
        );
    }
}
