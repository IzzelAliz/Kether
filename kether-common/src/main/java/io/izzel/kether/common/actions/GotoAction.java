package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class GotoAction<CTX extends QuestContext> implements QuestAction<Void, CTX> {

    private final String block;

    public GotoAction(String block) {
        this.block = block;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        context.setJump(block, 0);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String toString() {
        return "GotoAction{" +
            "block='" + block + '\'' +
            '}';
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(
            resolver -> new GotoAction<>(resolver.nextElement()),
            KetherCompleters.consume()
        );
    }
}
