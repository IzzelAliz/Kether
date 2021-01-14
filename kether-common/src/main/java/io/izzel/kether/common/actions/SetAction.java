package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class SetAction extends QuestAction<Void> {

    private final String key;
    private final String value;

    public SetAction(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext.Frame frame) {
        if (value == null || value.equals("null")) {
            frame.variables().set(key, null);
        } else {
            frame.variables().set(key, value);
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
            resolver -> new SetAction(resolver.nextToken(), resolver.nextToken()),
            KetherCompleters.seq(
                KetherCompleters.consume(),
                KetherCompleters.consume()
            )
        );
    }
}
