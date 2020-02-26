package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class CallAction<CTX extends QuestContext> implements QuestAction<Void, CTX> {

    private final String block;

    public CallAction(String block) {
        this.block = block;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean isPersist() {
        return true;
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        QuestContext child = context.createChild("call");
        child.setJump(block, 0);
        return child.runActions();
    }

    @Override
    public String getDataPrefix() {
        return "call_" + block;
    }

    @Override
    public String toString() {
        return "CallAction{" +
            "block='" + block + '\'' +
            '}';
    }

    public static <C extends QuestContext> QuestActionParser parser() {
        return QuestActionParser.<Void, C>of(
            resolver -> new CallAction<>(resolver.nextElement()),
            KetherCompleters.consume()
        );
    }
}
