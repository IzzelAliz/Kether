package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class GotoAction extends QuestAction<Void> {

    private final String block;

    public GotoAction(String block) {
        this.block = block;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext.Frame frame) {
        frame.setNext(frame.context().getQuest().getBlocks().get(block));
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
            resolver -> new GotoAction(resolver.nextToken()),
            KetherCompleters.consume()
        );
    }
}
