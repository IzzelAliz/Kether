package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class CallAction implements QuestAction<Object> {

    private final String block;

    public CallAction(String block) {
        this.block = block;
    }

    @Override
    public CompletableFuture<Object> process(QuestContext context) {
        QuestContext child = context.createChild();
        Quest.Block block = child.getQuest().getBlocks().get(this.block);
        child.setBlock(block);
        context.addClosable(child::terminate);
        return child.runActions();
    }

    @Override
    public String toString() {
        return "CallAction{" +
            "block='" + block + '\'' +
            '}';
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(
            resolver -> new CallAction(resolver.nextElement()),
            KetherCompleters.consume()
        );
    }
}
