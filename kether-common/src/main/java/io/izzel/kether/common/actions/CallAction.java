package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class CallAction extends QuestAction<Object> {

    private final String block;

    public CallAction(String block) {
        this.block = block;
    }

    @Override
    public CompletableFuture<Object> process(QuestContext.Frame frame) {
        QuestContext.Frame newFrame = frame.newFrame(block);
        newFrame.setNext(frame.context().getQuest().getBlocks().get(block));
        frame.addClosable(newFrame);
        return newFrame.run();
    }

    @Override
    public String toString() {
        return "CallAction{" +
            "block='" + block + '\'' +
            '}';
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(
            resolver -> new CallAction(resolver.nextToken()),
            KetherCompleters.consume()
        );
    }
}
